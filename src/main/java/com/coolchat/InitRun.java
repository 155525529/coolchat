package com.coolchat;

import com.coolchat.websocket.netty.NettyWebSocketStarter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitRun implements ApplicationRunner {
    private final DataSource dataSource;
    private final RedisTemplate redisTemplate;
    private final NettyWebSocketStarter nettyWebSocketStarter;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            dataSource.getConnection();
            redisTemplate.getConnectionFactory().getConnection().ping();
            new Thread(nettyWebSocketStarter).start();

            log.info("服务启动成功");

        } catch (SQLException e) {
            log.info("数据库配置错误");
        } catch (RedisConnectionFailureException e) {
            log.info("redis配置错误");
        } catch (Exception e) {
            log.info("服务器启动失败", e);
        }

    }
}
