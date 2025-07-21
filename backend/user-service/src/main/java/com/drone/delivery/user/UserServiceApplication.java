package com.drone.delivery.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 *
 * @author drone-delivery
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.drone.delivery")
@EnableDiscoveryClient
@MapperScan("com.drone.delivery.user.mapper")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}