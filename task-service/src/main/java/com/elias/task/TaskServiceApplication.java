package com.elias.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.elias.task.mapper")
@SpringBootApplication(scanBasePackages = {"com.elias.task", "com.elias.common"})
/**
 * 文件说明： TaskServiceApplication.
 * 组件职责： 项目中的通用组件。
 */
public class TaskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
