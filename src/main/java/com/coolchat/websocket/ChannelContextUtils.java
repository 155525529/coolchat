package com.coolchat.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.coolchat.constants.ChatConstants;
import com.coolchat.domain.dto.MessageSendDTO;
import com.coolchat.domain.dto.WsInitDataDTO;
import com.coolchat.domain.po.*;
import com.coolchat.domain.vo.ChatSessionUserVO;
import com.coolchat.enums.MessageType;
import com.coolchat.enums.UserContactApplyStatus;
import com.coolchat.enums.UserContactType;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.mapper.ChatSessionUserMapper;
import com.coolchat.mapper.UserInfoMapper;
import com.coolchat.util.RedisComponent;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChannelContextUtils {
    private final RedisComponent redisComponent;
    private final UserInfoMapper userInfoMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;

    private static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();

        AttributeKey attributeKey = AttributeKey.valueOf(channelId);
        channel.attr(attributeKey).set(userId);

        List<String> contactIdList = redisComponent.getUserContactIdList(userId);
        for (String id : contactIdList) {
            if (id.startsWith(UserContactType.Group.getPrefix())) {
                add2Group(id, channel);
            }
        }

        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveHeartBeat(userId);

        //更新用户最后连接时间
        Db.lambdaUpdate(UserInfo.class)
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getLastLoginTime, LocalDateTime.now())
                .update();

        //给用户发送消息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        LocalDateTime lastOffTime = userInfo.getLastOffTime();
        Duration between = Duration.between(lastOffTime, LocalDateTime.now());
        //最多查询并发送三天前的未读消息
        if (between.getSeconds() > ChatConstants.MAXIMUM_TIME_OF_MESSAGE){
            lastOffTime = LocalDateTime.now().minusDays(3);
        }

        //1.查询用户所有会话信息
        List<ChatSessionUserVO> chatSessionUserList = chatSessionUserMapper.getChatSessionInfo(userId);

        WsInitDataDTO wsInitDataDTO = new WsInitDataDTO();
        wsInitDataDTO.setChatSessionList(chatSessionUserList);

        //2.查询聊天消息
        List<String> groupIdList = contactIdList.stream().filter(item -> item.startsWith(UserContactType.Group.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);
        List<ChatMessage> chatMessageList = Db.lambdaQuery(ChatMessage.class)
                .in(ChatMessage::getContactId, groupIdList)
                .gt(ChatMessage::getSendTime, lastOffTime)
                .list();
        wsInitDataDTO.setChatMessageList(chatMessageList);
        //3.查询好友申请
        Long applyCount = Db.lambdaQuery(UserContactApply.class)
                .eq(UserContactApply::getReceiveUserId, userId)
                .gt(UserContactApply::getLastApplyTime, lastOffTime)
                .eq(UserContactApply::getStatus, UserContactApplyStatus.INIT)
                .count();
        Integer count = applyCount != null ? applyCount.intValue() : null;
        wsInitDataDTO.setApplyCount(count);

        //发送消息
        MessageSendDTO messageSendDTO = MessageSendDTO.builder()
                .messageType(MessageType.INIT)
                .contactId(userId)
                .extendData(wsInitDataDTO)
                .build();

        sendMsg2User(messageSendDTO, userId);

    }

    public void addUser2Group(String userId, String groupId) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        add2Group(groupId, channel);
    }
    private void add2Group(String groupId, Channel channel) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if (channel == null) {
            return;
        }
        group.add(channel);
    }

    public void removeContext(Channel channel){
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if (!userId.isEmpty()){
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeHeartBeat(userId);

        //更新用户最后离线时间
        Db.lambdaUpdate(UserInfo.class)
                .eq(UserInfo::getUserId, userId)
                .set(UserInfo::getLastOffTime, LocalDateTime.now())
                .update();

    }

    public void sendMessage(MessageSendDTO messageSendDTO){
        String contactId = messageSendDTO.getContactId();
        if (StrUtil.isEmpty(contactId)){
            return;
        }
        if (UserContactType.USER.equals(UserContactType.getById(contactId))){
            //发送消息给个人
            sendMsg2User(messageSendDTO, contactId);
            //强制下线
            if (MessageType.FORCE_OFF_LINE.equals(messageSendDTO.getMessageType())){
                closeContact(contactId);
            }

        }else if (UserContactType.Group.equals(UserContactType.getById(contactId))){
            //发送消息给群组
            ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(messageSendDTO.getContactId());
            if (channelGroup == null){
                return;
            }
            channelGroup.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(messageSendDTO)));

            //移除群聊
            if (MessageType.LEAVE_GROUP.equals(messageSendDTO.getMessageType()) || MessageType.REMOVE_GROUP.equals(messageSendDTO.getMessageType())){
                String userId = (String) messageSendDTO.getExtendData();
                redisComponent.removeUserContact(userId, messageSendDTO.getContactId());
                Channel channel = USER_CONTEXT_MAP.get(userId);
                if (channel == null){
                    return;
                }
                channelGroup.remove(channel);
            }
            if (MessageType.DISSOLUTION_GROUP.equals(messageSendDTO.getMessageType())){
                GROUP_CONTEXT_MAP.remove(messageSendDTO.getContactId());
                channelGroup.close();
            }

        }else {
            throw new BizIllegalException("未知类型");
        }
    }

    public void closeContact(String userId){
        if (StrUtil.isEmpty(userId)){
            return;
        }
        redisComponent.cleanUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null){
            return;
        }
        channel.close();
    }

    //发送消息
    public static void sendMsg2User(MessageSendDTO messageSendDTO, String receiveId){
        if (receiveId == null){
            return;
        }
        Channel userChannel = USER_CONTEXT_MAP.get(receiveId);
        if (userChannel == null){
            return;
        }
        //相对客户端而言，联系人就是发送人，所以这里转一下再发送
        if (MessageType.ADD_FRIEND_SELF.equals(messageSendDTO.getMessageType())){
            UserInfo contactUserInfo = (UserInfo)messageSendDTO.getExtendData();
            messageSendDTO.setMessageType(MessageType.ADD_FRIEND);
            messageSendDTO.setContactId(contactUserInfo.getUserId());
            messageSendDTO.setContactName(contactUserInfo.getNickName());
            messageSendDTO.setExtendData(null);
        }else {
            messageSendDTO.setContactId(messageSendDTO.getSendUserId());
            messageSendDTO.setContactName(messageSendDTO.getSendUserNickName());
        }
        userChannel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(messageSendDTO)));
    }

}
