package com.coolchat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.coolchat"})
@MapperScan(basePackages = {"com.coolchat.mapper"})
@EnableTransactionManagement
@EnableScheduling
public class CoolChatApplication {
    public static void main(String[] args) {
         SpringApplication.run(CoolChatApplication.class,args);
    }
}
