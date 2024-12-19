package com.coolchat.websocket;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.coolchat.domain.dto.MessageSendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageHandler {
    private final RedissonClient redissonClient;
    private final ChannelContextUtils channelContextUtils;

    private static final String MESSAGE_TOPIC = "message.topic";

    @PostConstruct
    public void lisMessage() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDTO.class, (MessageSendDTO, sendDTO) -> {
            try {
                log.info("收到广播消息：{}", JSONUtil.toJsonStr(sendDTO));
                channelContextUtils.sendMessage(sendDTO);
            } catch (Exception e) {
                log.error("处理广播消息时发生异常: {}", e.getMessage(), e);
            }
        });
    }

    public void sendMessage(MessageSendDTO sendDTO) {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(sendDTO);
    }
}
