package com.coolchat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.coolchat.config.AppConfig;
import com.coolchat.constants.FileConstants;
import com.coolchat.domain.bo.ChatSessionBO;
import com.coolchat.domain.dto.*;
import com.coolchat.domain.po.*;
import com.coolchat.domain.vo.GroupInfoVO;
import com.coolchat.enums.*;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.exception.ForbiddenException;
import com.coolchat.mapper.*;
import com.coolchat.result.Result;
import com.coolchat.service.IGroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolchat.util.ChatMessageUtils;
import com.coolchat.util.RedisComponent;
import com.coolchat.websocket.ChannelContextUtils;
import com.coolchat.websocket.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
@Service
@RequiredArgsConstructor
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements IGroupInfoService {

    private final GroupInfoMapper groupInfoMapper;
    private final RedisTemplate redisTemplate;
    private final RedisComponent redisComponent;
    private final UserContactMapper userContactMapper;
    private final AppConfig appConfig;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;
    private final ChannelContextUtils channelContextUtils;
    private final ChatMessageMapper chatMessageMapper;
    private final MessageHandler messageHandler;
    private final UserContactApplyServiceImpl userContactApplyService;
    private final UserInfoMapper userInfoMapper;
    @Lazy
    private GroupInfoServiceImpl groupInfoService;

    @Override
    @Transactional
    public Result saveGroup(GroupDTO groupDTO, HttpServletRequest request) throws IOException {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        GroupInfo groupInfo = GroupInfo.builder()
                .groupId(groupDTO.getGroupId())
                .groupName(groupDTO.getGroupName())
                .groupNotice(groupDTO.getGroupNotice())
                .groupOwnerId(tokenUserInfoDTO.getUserId())
                .joinType(JoinType.getByType(groupDTO.getJoinType()))
                .status(GroupContactStatus.NORMAL)
                .build();
        //判断群组id是否为空
        if (StrUtil.isEmpty(groupInfo.getGroupId())){
            createGroup(groupInfo, groupDTO);
        }else {
            updateGroup(groupInfo);
        }

        String baseFolder = appConfig.getProjectFolder() + FileConstants.FILE_FOLDER;
        File targetFileFolder = new File(baseFolder + FileConstants.AVATAR_FOLDER);
        if (!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getGroupId() + FileConstants.IMAGE_SUFFIX;
        groupDTO.getAvatarFile().transferTo(new File(filePath));
        groupDTO.getAvatarCover().transferTo(new File(filePath + FileConstants.COVER_IMAGE_SUFFIX));

        return Result.success();
    }

    private void createGroup(GroupInfo groupInfo, GroupDTO groupDTO){

        LambdaQueryWrapper wrapper = new LambdaQueryWrapper<GroupInfo>().eq(GroupInfo::getGroupOwnerId, groupInfo.getGroupOwnerId());
        Long count = groupInfoMapper.selectCount(wrapper);
        SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();
        //判断是否到达群创建上限
        if (count >= sysSettingDTO.getMaxGroupCount()){
            throw new BizIllegalException("最多支持创建" + sysSettingDTO.getMaxGroupCount() + "个群聊");
        }
        //判断群头像是否缺失
        if (groupDTO.getAvatarFile() == null){
            throw new BizIllegalException("文件缺失");
        }
        groupInfo.setCreateTime(LocalDateTime.now());
        groupInfo.setGroupId(UserContactType.Group.getPrefix() + RandomUtil.randomNumbers(11));
        groupInfoMapper.insert(groupInfo);

        //将群组添加为联系人
        UserContact userContact = UserContact.builder()
                .status(UserContactStatus.FRIEND)
                .userId(groupInfo.getGroupOwnerId())
                .contactType(UserContactType.Group)
                .createTime(groupInfo.getCreateTime())
                .lastUpdateTime(groupInfo.getCreateTime())
                .contactId(groupInfo.getGroupId())
                .build();
        userContactMapper.insert(userContact);

        //创键会话
        String sessionId = ChatMessageUtils.getChatSessionId4Group(groupInfo.getGroupId());
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionId)
                .lastMessage(MessageType.GROUP_CREATE.getInitMessage())
                .lastReceiveTime(LocalDateTime.now())
                .build();
        chatSessionMapper.insert(chatSession);

        ChatSessionUser chatSessionUser = ChatSessionUser.builder()
                .userId(groupInfo.getGroupOwnerId())
                .contactId(groupInfo.getGroupId())
                .contactName(groupInfo.getGroupName())
                .sessionId(sessionId)
                .build();
        chatSessionUserMapper.insert(chatSessionUser);

        //创建消息
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .messageType(MessageType.GROUP_CREATE)
                .messageContent(MessageType.GROUP_CREATE.getInitMessage())
                .sendTime(LocalDateTime.now())
                .contactId(groupInfo.getGroupId())
                .contactType(UserContactType.Group)
                .status(MessageStatus.SENT)
                .build();
        chatMessageMapper.insert(chatMessage);

        //将群组添加到联系人
        redisComponent.addUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());
        //将联系人通道添加到群组通道
        channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(), groupInfo.getGroupId());

        //发送ws消息
        ChatSessionBO chatSessionBO = BeanUtil.copyProperties(chatSessionUser, ChatSessionBO.class);
        chatSessionBO.setLastMessage(MessageType.GROUP_CREATE.getInitMessage());
        chatSessionBO.setLastReceiveTime(LocalDateTime.now());
        chatSessionBO.setMemberCount(1);

        MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
        messageSendDTO.setExtendData(chatSessionBO);
        messageSendDTO.setLastMessage(chatSessionBO.getLastMessage());
        messageHandler.sendMessage(messageSendDTO);
    }

    private void updateGroup(GroupInfo groupInfo){

        String groupId = groupInfo.getGroupId();
        String groupName = groupInfo.getGroupName();
        String groupNotice = groupInfo.getGroupNotice();
        JoinType joinType = groupInfo.getJoinType();

        //判断是否为群主
        GroupInfo dbInfo = groupInfoMapper.selectById(groupId);
        if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())){
            throw new ForbiddenException("权限不足");
        }
        groupInfoMapper.updateById(groupInfo);

        //更新相关冗余信息

        lambdaUpdate().eq(GroupInfo::getGroupId, groupId)
                .set(groupName != null ,GroupInfo::getGroupName, groupName)
                .set(groupNotice != null, GroupInfo::getGroupNotice, groupNotice)
                .set(joinType != null, GroupInfo::getJoinType, joinType)
                .update();
        Db.lambdaUpdate(ChatSessionUser.class)
                .eq(ChatSessionUser::getContactId, groupId)
                .set(groupName != null ,ChatSessionUser::getContactName, groupName)
                .update();

        //发送ws消息
        MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                .contactType(UserContactType.Group)
                .contactId(groupId)
                .extendData(groupInfo)
                .messageType(MessageType.GROUP_NAME_UPDATE)
                .build();
        messageHandler.sendMessage(messageSendDTO);

    }

    /**
     * 获取群组详情
     * @param request
     * @param groupId
     * @return
     */
    @Override
    public Result<GroupInfo> getGroupInfo(HttpServletRequest request, String groupId) {
        GroupInfo groupInfo = getGroupInfoMethod(request, groupId);

        return Result.success(groupInfo);
    }

    @Override
    public Result<GroupInfoVO> getGroupInfo4Chat(HttpServletRequest request, String groupId) {
        GroupInfo groupInfo = getGroupInfoMethod(request, groupId);
        //SELECT c.*, u.nick_name,u.sex FROM user_contact c INNER JOIN user_info u ON u.user_id = c.user_id WHERE contact_id = ""
        List<UserContact> userContactList = userContactMapper.getListOfContact(groupId, UserContactStatus.FRIEND);
        GroupInfoVO groupInfoVO = GroupInfoVO.builder()
                .groupInfo(groupInfo)
                .userContactList(userContactList)
                .build();
        return Result.success(groupInfoVO);
    }

    private GroupInfo getGroupInfoMethod(HttpServletRequest request, String groupId){
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);

        UserContact contact = userContactMapper.getByUserIdAndContactId(tokenUserInfoDTO.getUserId(), groupId);
        //验证查询者身份信息与被查询群聊信息
        if(contact == null || !UserContactStatus.FRIEND.equals(contact.getStatus())){
            throw new BizIllegalException("你不在群聊或群聊不存在");
        }
        GroupInfo groupInfo = this.lambdaQuery().eq(GroupInfo::getGroupId, groupId).one();
        if (groupInfo == null || !GroupContactStatus.NORMAL.equals(groupInfo.getStatus())){
            throw new BizIllegalException("群聊不存在或已解散");
        }
        return groupInfo;
    }

    /**
     * 解散群聊
     * @param groupId
     * @return
     */
    @Override
    public Result dissolutionGroup(String groupId, TokenUserInfoDTO tokenUserInfoDTO, Boolean isAdmin) {
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null || GroupContactStatus.DISSOLVE.equals(groupInfo.getStatus())){
            throw new BizIllegalException("未找到群聊");
        }
        if (!(isAdmin || groupInfo.getGroupOwnerId().equals(tokenUserInfoDTO.getUserId()))){
            throw new BizIllegalException("非法操作");
        }

        //设置群状态为已解散
        lambdaUpdate().eq(GroupInfo::getGroupId, groupId)
                .set(GroupInfo::getStatus, GroupContactStatus.DISSOLVE)
                .update();

        //更新联系人信息
        LambdaUpdateWrapper<UserContact> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserContact::getContactId, groupId)
                .eq(UserContact::getContactType, UserContactType.Group)
                .set(UserContact::getStatus, UserContactStatus.BE_DEL);
        userContactMapper.update(null, wrapper);

        //移除相关群员联系人缓存
        List<UserContact> userContactList = Db.lambdaQuery(UserContact.class)
                .eq(UserContact::getContactId, groupId)
                .eq(UserContact::getContactType, UserContactType.Group)
                .eq(UserContact::getStatus, UserContactStatus.FRIEND)
                .list();
        for (UserContact usercontact : userContactList) {
            redisComponent.removeUserContact(usercontact.getUserId(), usercontact.getContactId());
        }

        //更新会话消息
        String sessionId = ChatMessageUtils.getChatSessionId4Group(groupId);
        String messageContent = MessageType.DISSOLUTION_GROUP.getInitMessage();

        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionId)
                .lastMessage(messageContent)
                .lastReceiveTime(LocalDateTime.now())
                .build();
        chatSessionMapper.updateById(chatSession);

        //发送群已解散消息
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .contactId(groupId)
                .sendUserNickName(groupInfo.getGroupName())
                .sendTime(LocalDateTime.now())
                .contactType(UserContactType.Group)
                .status(MessageStatus.SENT)
                .messageType(MessageType.DISSOLUTION_GROUP)
                .contactId(groupId)
                .messageContent(messageContent)
                .build();
        chatMessageMapper.insert(chatMessage);
        MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
        messageHandler.sendMessage(messageSendDTO);

        return Result.success();
    }

    /**
     * 添加或移除群成员
     * @param tokenUserInfoDTO
     * @param groupId
     * @param contactIds
     * @param opType
     * @return
     */
    @Override
    public Result addOrRemoveGroupUser(TokenUserInfoDTO tokenUserInfoDTO, String groupId, String contactIds, Integer opType) {
        OperationType operationType = OperationType.getByType(opType);
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null || !groupInfo.getGroupOwnerId().equals(tokenUserInfoDTO.getUserId())){
            throw new BizIllegalException("非法操作");
        }

        String[] contactIdList = contactIds.split(",");
        for(String contactId : contactIdList){
            if (OperationType.REMOVE.equals(operationType)){
                groupInfoService.leaveGroup(contactId, groupId, MessageType.REMOVE_GROUP);

            }else if (OperationType.ADD.equals(operationType)){
                AddContactDTO dto = AddContactDTO.builder()
                        .applyUserId(contactId)
                        .contactId(groupId)
                        .contactType(UserContactType.Group)
                        .applyInfo(tokenUserInfoDTO.getNickName() + "将你拉入群聊")
                        .build();
                userContactApplyService.addContact(dto);

            }else {
                throw new BizIllegalException("参数错误");
            }
        }


        return Result.success();
    }

    /**
     * 退出群聊
     * @param userId
     * @param groupId
     * @param messageType
     * @return
     */
    @Override
    @Transactional
    public Result leaveGroup(String userId, String groupId, MessageType messageType){
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        UserContact contact = Db.lambdaQuery(UserContact.class)
                .eq(UserContact::getUserId, userId)
                .eq(UserContact::getContactId, groupId)
                .eq(UserContact::getStatus, UserContactStatus.FRIEND)
                .one();
        if (groupInfo == null || userId.equals(groupInfo.getGroupOwnerId()) || contact == null){
            throw new BizIllegalException("参数错误");
        }

        //更新关系表
        Db.lambdaUpdate(UserContact.class)
                .eq(UserContact::getUserId, userId)
                .eq(UserContact::getContactId, groupId)
                .set(UserContact::getStatus, UserContactStatus.DEL)
                .set(UserContact::getLastUpdateTime, LocalDateTime.now())
                .update();

        UserInfo userInfo = userInfoMapper.selectById(userId);
        String sessionId = ChatMessageUtils.getChatSessionId4Group(groupId);
        String messageContact = String.format(messageType.getInitMessage(), userInfo.getNickName());
        ChatSession chatSession = ChatSession.builder()
                .lastMessage(messageContact)
                .lastReceiveTime(LocalDateTime.now())
                .sessionId(sessionId)
                .build();
        chatSessionMapper.updateById(chatSession);

        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .sendTime(LocalDateTime.now())
                .contactType(UserContactType.Group)
                .status(MessageStatus.SENT)
                .messageType(messageType)
                .contactId(groupId)
                .messageContent(messageContact)
                .build();
        chatMessageMapper.insert(chatMessage);

        Long memberCount = lambdaQuery()
                .eq(GroupInfo::getGroupId, groupId)
                .eq(GroupInfo::getStatus, UserContactStatus.FRIEND)
                .count();
        Integer count = memberCount.intValue();

        MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
        messageSendDTO.setMemberCount(count);
        messageSendDTO.setExtendData(userId);
        messageHandler.sendMessage(messageSendDTO);

        return Result.success();
    }
}
