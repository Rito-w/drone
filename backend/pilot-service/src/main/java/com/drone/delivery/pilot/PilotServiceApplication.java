package com.drone.delivery.pilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 飞手服务启动类
 * 
 * @author drone-delivery
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PilotServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PilotServiceApplication.class, args);
    }
}