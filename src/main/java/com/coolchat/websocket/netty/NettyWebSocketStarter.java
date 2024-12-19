package com.coolchat.websocket.netty;

import cn.hutool.core.util.StrUtil;
import com.coolchat.config.AppConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyWebSocketStarter implements Runnable{

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup = new NioEventLoopGroup();
    private final  AppConfig appConfig;
    private final HandlerWebSocket handlerWebSocket;

    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //设置几个重要的处理器
                            //对netty协议的支持,使用http编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            //聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            //保证接收到http请求完整性
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            //心跳 long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit
                            //readerIdleTime 读超时时间，即测试端一定时间内未接收到被测试端消息
                            //writerIdleTime 写超时时间，即测试端一定时间内向被测试端发送消息
                            //allIdleTime 所有类型超时时间
                            pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new HandlerHeartBeat());
                            //将http协议升级为ws协议，对websocket支持
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true, 100001));
                            pipeline.addLast(handlerWebSocket);
                        }
                    });
            Integer wsPort = appConfig.getWsPort();
            String wsPortStr = System.getProperty("ws.port");
            if (!StrUtil.isEmpty(wsPortStr)){
                wsPort = Integer.parseInt(wsPortStr);
            }

            ChannelFuture channelFuture = serverBootstrap.bind(wsPort).sync();
            log.info("netty启动成功，端口{}", appConfig.getWsPort());
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.info("启动netty失败", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
