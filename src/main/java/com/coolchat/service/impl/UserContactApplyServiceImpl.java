package com.coolchat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.coolchat.domain.dto.*;
import com.coolchat.domain.po.*;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.domain.vo.ApplySearchVO;
import com.coolchat.enums.*;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.mapper.*;
import com.coolchat.result.Result;
import com.coolchat.service.IUserContactApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolchat.util.ChatMessageUtils;
import com.coolchat.util.RedisComponent;
import com.coolchat.websocket.ChannelContextUtils;
import com.coolchat.websocket.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

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
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements IUserContactApplyService {

    private final UserInfoMapper userInfoMapper;
    private final GroupInfoMapper groupInfoMapper;
    private final UserContactMapper userContactMapper;
    private final RedisComponent redisComponent;
    private final UserContactApplyMapper userContactApplyMapper;
    private final MessageHandler messageHandler;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChannelContextUtils channelContextUtils;


    /**
     * 申请添加联系人
     * @param request
     * @param contactId
     * @param applyInfo
     * @return
     */
    @Override
    @Transactional
    public Result applyAdd(HttpServletRequest request, String contactId, String applyInfo) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        UserContactType contactType = UserContactType.getById(contactId);
        if (contactType == null){
            throw new BizIllegalException("申请异常");
        }

        String applyUserId = tokenUserInfoDTO.getUserId();

        JoinType joinType = null;
        String reseiveUserId = contactId;


        //如被对方拉黑无法添加
        UserContact userContact = userContactMapper.getByUserIdAndContactId(applyUserId, contactId);
        if (userContact != null &&
                (UserContactStatus.BE_BLOCK.equals(userContact.getStatus()) || UserContactStatus.BE_BLOCK_FIRST.equals(userContact.getStatus()))){
            throw new BizIllegalException("对方已将你拉黑");
        }

        //如未找到对方或群聊人数已满则无法添加
        if (UserContactType.USER.equals(contactType)){

            UserInfo userInfo = userInfoMapper.selectById(contactId);
            if (userInfo == null){
                throw new BizIllegalException("用户不存在");
            }
            joinType = userInfo.getJoinType();

        }else if (UserContactType.Group.equals(contactType)){

            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();
            if (groupInfo == null || GroupContactStatus.DISSOLVE.equals(groupInfo.getStatus())){
                throw new BizIllegalException("群聊不存在");
            }
            Long memberCount = Db.lambdaQuery(UserContact.class)
                    .eq(UserContact::getContactId, contactId)
                    .eq(UserContact::getStatus, UserContactStatus.FRIEND)
                    .count();
            if(memberCount >= sysSettingDTO.getMaxGroupMemberCount()){
                throw new BizIllegalException("群聊人数达到上限");
            }
            reseiveUserId = groupInfo.getGroupOwnerId();
            joinType = groupInfo.getJoinType();

        }else {
            throw new BizIllegalException("未知属性");
        }

        //直接加入不记录申请
        if (JoinType.NO_CHECK.equals(joinType)){
            //添加到联系人
            AddContactDTO dto = AddContactDTO.builder()
                    .applyUserId(applyUserId)
                    .contactId(contactId)
                    .contactType(contactType)
                    .applyInfo(applyInfo)
                    .build();
            addContact(dto);
            return Result.success(joinType);
        }

        //发送申请
        QueryWrapper<UserContactApply> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(UserContactApply::getApplyUserId, applyUserId)
                .eq(UserContactApply::getReceiveUserId, reseiveUserId)
                .eq(UserContactApply::getContactId, contactId);
        UserContactApply userContactApply = userContactApplyMapper.selectOne(wrapper);
        //如果之前未申请过，则插入申请表
        if (userContactApply == null){
            UserContactApply contactApply = UserContactApply.builder()
                    .applyUserId(applyUserId)
                    .receiveUserId(reseiveUserId)
                    .contactType(contactType)
                    .contactId(contactId)
                    .lastApplyTime(LocalDateTime.now())
                    .status(UserContactApplyStatus.INIT)
                    .applyInfo(applyInfo)
                    .build();
            userContactApplyMapper.insert(contactApply);
        }else {
            //如果之前申请过则更新状态
            UserContactApply contactApply = UserContactApply.builder()
                    .status(UserContactApplyStatus.INIT)
                    .lastApplyTime(LocalDateTime.now())
                    .applyInfo(applyInfo)
                    .build();
            UpdateWrapper<UserContactApply> wrapper1 = new UpdateWrapper<>();
            wrapper1.lambda().eq(UserContactApply::getApplyId, userContactApply.getApplyId());
            userContactApplyMapper.update(contactApply, wrapper1);
        }

        if (userContactApply == null || !UserContactApplyStatus.INIT.equals(userContactApply.getStatus())){
            //发送ws消息
            MessageSendDTO messageSendDTO = MessageSendDTO.builder()
                    .messageType(MessageType.CONTACT_APPLY)
                    .messageContent(applyInfo)
                    .contactId(reseiveUserId)
                    .build();
            messageHandler.sendMessage(messageSendDTO);
        }

        return Result.success(joinType);
    }

    /**
     * 获取好友申请列表
     *
     * @param request
     * @param query
     * @return
     */
    @Override
    public Result<PageDTO<ApplySearchVO>> loadApply(HttpServletRequest request, PageQuery query) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);

        Page<ApplySearchVO> page = query.toMpPage("last_apply_time", false);
        userContactApplyMapper.getApplyInfo(page, tokenUserInfoDTO.getUserId());
        PageDTO<ApplySearchVO> of = PageDTO.of(page, ApplySearchVO.class);

        return Result.success(of);
    }

    /**
     * 处理联系人申请
     * @param request
     * @param applyId
     * @param status
     * @return
     */
    @Override
    @Transactional
    public Result dealWithApply(HttpServletRequest request, Integer applyId, Integer status) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        UserContactApplyStatus statusEnum = UserContactApplyStatus.getByStatus(status);
        if (statusEnum == null || UserContactApplyStatus.INIT.equals(statusEnum)){
            throw new BizIllegalException("错误输入");
        }
        UserContactApply dbApply = userContactApplyMapper.selectById(applyId);
        if (dbApply == null || !tokenUserInfoDTO.getUserId().equals(dbApply.getReceiveUserId())){
            throw new BizIllegalException("未找到待处理申请");
        }
        //更新申请表
        this.lambdaUpdate().eq(UserContactApply::getApplyId, applyId)
                .eq(UserContactApply::getStatus, UserContactApplyStatus.INIT)
                .set(UserContactApply::getStatus, statusEnum)
                .set(UserContactApply::getLastApplyTime, LocalDateTime.now())
                .update();

        //通过
        if (UserContactApplyStatus.PASS.equals(statusEnum)){
            //添加联系人
            AddContactDTO dto = AddContactDTO.builder()
                    .applyUserId(dbApply.getApplyUserId())
                    .contactId(dbApply.getContactId())
                    .contactType(dbApply.getContactType())
                    .applyInfo(dbApply.getApplyInfo())
                    .build();
            addContact(dto);
            return Result.success();
        }

        //拉黑
        if (UserContactApplyStatus.BLACKLIST.equals(statusEnum)){
            //设置自己与申请者关系为拉黑
            UserContact userContact = UserContact.builder()
                    .userId(tokenUserInfoDTO.getUserId())
                    .contactId(dbApply.getApplyUserId())
                    .contactType(dbApply.getContactType())
                    .createTime(LocalDateTime.now())
                    .status(UserContactStatus.BLOCK)
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
            userContactMapper.insert(userContact);
            //设置申请者与自己关系为被拉黑
            userContact.setUserId(dbApply.getApplyUserId());
            userContact.setStatus(UserContactStatus.BE_BLOCK_FIRST);
            userContact.setContactId(tokenUserInfoDTO.getUserId());
            userContactMapper.insert(userContact);
        }

        return Result.success();
    }

    public void addContact(AddContactDTO addContactDTO){

        String applyUserId = addContactDTO.getApplyUserId();
        String contactId = addContactDTO.getContactId();
        UserContactType contactType = addContactDTO.getContactType();
        String applyInfo = addContactDTO.getApplyInfo();

        //申请人添加对方
        UserContact userContact = UserContact.builder()
                .userId(applyUserId)
                .contactId(contactId)
                .contactType(contactType)
                .status(UserContactStatus.FRIEND)
                .createTime(LocalDateTime.now())
                .lastUpdateTime(LocalDateTime.now())
                .build();
        userContactMapper.insert(userContact);

        //如果是添加好友，则接收人添加申请人，添加缓存
        if (UserContactType.USER.equals(contactType)){
            userContact.setUserId(contactId);
            userContact.setContactId(applyUserId);
            userContactMapper.insert(userContact);

            redisComponent.addUserContact(contactId, applyUserId);
        }
        redisComponent.addUserContact(applyUserId, contactId);

        //创建会话，发送消息
        String sessionId = null;
        if (UserContactType.USER.equals(contactType)){
            sessionId = ChatMessageUtils.getChatSessionId4User(new String[]{applyUserId, contactId});
        }else if (UserContactType.Group.equals(contactType)){
            sessionId = ChatMessageUtils.getChatSessionId4Group(contactId);
        }else {
            throw new BizIllegalException("未知类型");
        }

        if (UserContactType.USER.equals(contactType)){
            //创建会话
            ChatSession chatSession = ChatSession.builder()
                    .sessionId(sessionId)
                    .lastMessage(applyInfo)
                    .lastReceiveTime(LocalDateTime.now())
                    .build();
            chatSessionMapper.insert(chatSession);

            //申请人session
            UserInfo contactUserInfo = userInfoMapper.selectById(contactId);
            ChatSessionUser applySessionUser = ChatSessionUser.builder()
                    .userId(applyUserId)
                    .contactId(contactId)
                    .contactName(contactUserInfo.getNickName())
                    .sessionId(sessionId)
                    .build();
            chatSessionUserMapper.insert(applySessionUser);

            //接受人session
            UserInfo applyUserInfo = userInfoMapper.selectById(applyUserId);
            ChatSessionUser contactSessionUser = ChatSessionUser.builder()
                    .userId(contactId)
                    .contactId(applyUserId)
                    .contactName(applyUserInfo.getNickName())
                    .sessionId(sessionId)
                    .build();
            chatSessionUserMapper.insert(contactSessionUser);

            //记录消息表
            ChatMessage chatMessage = ChatMessage.builder()
                    .sessionId(sessionId)
                    .messageType(MessageType.ADD_FRIEND)
                    .messageContent(applyInfo)
                    .sendUserId(applyUserId)
                    .sendUserNickName(applyUserInfo.getNickName())
                    .sendTime(LocalDateTime.now())
                    .contactId(contactId)
                    .contactType(UserContactType.USER)
                    .build();
            chatMessageMapper.insert(chatMessage);

            MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
            //发送给接受和申请者
            messageHandler.sendMessage(messageSendDTO);

            //发送给申请人，发送人就是接受人，联系人就是申请人
            messageSendDTO.setMessageType(MessageType.ADD_FRIEND_SELF);
            messageSendDTO.setContactId(applyUserId);
            messageSendDTO.setExtendData(contactUserInfo);
            messageHandler.sendMessage(messageSendDTO);

        }else {
            //加入群组
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            ChatSessionUser chatSessionUser = ChatSessionUser.builder()
                    .userId(applyUserId)
                    .contactId(contactId)
                    .contactName(groupInfo.getGroupName())
                    .sessionId(sessionId)
                    .build();
            //更新session信息
            if (Db.lambdaQuery(ChatSessionUser.class).eq(ChatSessionUser::getSessionId, sessionId).exists()){
                LambdaUpdateWrapper<ChatSessionUser> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(ChatSessionUser::getSessionId, sessionId);
                chatSessionUserMapper.update(chatSessionUser,wrapper);
            }else {
                chatSessionUserMapper.insert(chatSessionUser);
            }

            UserInfo applyUserInfo = userInfoMapper.selectById(applyUserId);
            String sendMessage = String.format(MessageType.ADD_GROUP.getInitMessage(), applyUserInfo.getNickName());

            ChatSession chatSession = ChatSession.builder()
                    .sessionId(sessionId)
                    .lastReceiveTime(LocalDateTime.now())
                    .lastMessage(sendMessage)
                    .build();
            if (Db.lambdaQuery(ChatSession.class).eq(ChatSession::getSessionId, sessionId).exists()) {
                chatSessionMapper.updateById(chatSession);
            } else {
                chatSessionMapper.insert(chatSession);
            }

            //增加聊天信息
            ChatMessage chatMessage = ChatMessage.builder()
                    .sessionId(sessionId)
                    .messageType(MessageType.ADD_GROUP)
                    .messageContent(sendMessage)
                    .sendTime(LocalDateTime.now())
                    .contactId(contactId)
                    .contactType(UserContactType.Group)
                    .status(MessageStatus.SENT)
                    .build();
            chatMessageMapper.insert(chatMessage);

            //将群组添加到联系人
            redisComponent.addUserContact(applyUserId, groupInfo.getGroupId());
            //将联系人通道添加到群组通道
            channelContextUtils.addUser2Group(applyUserId, groupInfo.getGroupId());

            //发送群消息
            MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
            Long memberCount = Db.lambdaQuery(UserContact.class)
                    .eq(UserContact::getContactId, contactId)
                    .eq(UserContact::getStatus, UserContactStatus.FRIEND)
                    .count();
            Integer count = Convert.toInt(memberCount);
            messageSendDTO.setMemberCount(count);
            messageSendDTO.setContactName(groupInfo.getGroupName());

            //发消息
            messageHandler.sendMessage(messageSendDTO);
        }
    }
}
