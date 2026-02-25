package com.elias.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// 1) 开启异步能力：允许使用线程池执行异步任务
@EnableAsync
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
        // 1.1) 启动 task-service 应用上下文
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
