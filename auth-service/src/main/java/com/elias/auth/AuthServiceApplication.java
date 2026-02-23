package com.elias.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.elias.auth.mapper")
@SpringBootApplication(scanBasePackages = {"com.elias.auth", "com.elias.common"})
/**
 * 文件说明： AuthServiceApplication.
 * 组件职责： 项目中的通用组件。
 */
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
