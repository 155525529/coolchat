package com.coolchat.websocket.netty;

import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.util.RedisComponent;
import com.coolchat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final RedisComponent redisComponent;
    private final ChannelContextUtils channelContextUtils;

    /**
     * 通道就绪后调用，一般来做初始化
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接加入...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有连接断开...");
        channelContextUtils.removeContext(ctx.channel());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        log.info("收到userId:{}的消息：{}", userId, textWebSocketFrame.text());
        redisComponent.saveHeartBeat(userId);
    }

    //处理由用户触发的事件
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToken(url);
            if (token == null){
                ctx.channel().close();
                return;
            }
            TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(token);
            if (tokenUserInfoDTO == null){
                ctx.channel().close();
                return;
            }

//            redisComponent.saveChannel(tokenUserInfoDTO.getUserId(), ctx.channel());
            channelContextUtils.addContext(tokenUserInfoDTO.getUserId(), ctx.channel());

        }
    }

    private String getToken(String url){
        if (url.isEmpty() || !url.contains("?")){
            return null;
        }
        String[] queryParam = url.split("\\?");
        if (queryParam.length != 2){
            return null;
        }
        String[] param = queryParam[1].split("=");
        if (param.length != 2){
            return null;
        }
        return param[1];
    }
}
