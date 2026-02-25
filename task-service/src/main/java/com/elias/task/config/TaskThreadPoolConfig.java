package com.elias.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class TaskThreadPoolConfig {

    @Bean("taskQueryExecutor")
    public Executor taskQueryExecutor() {
        // 1) 创建业务线程池：用于任务查询并行化
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 1.1) 核心线程数：常驻工作线程
        executor.setCorePoolSize(4);
        // 1.2) 最大线程数：高峰时可扩容上限
        executor.setMaxPoolSize(8);
        // 1.3) 任务队列容量：缓存待执行任务
        executor.setQueueCapacity(200);
        // 1.4) 非核心线程空闲存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 1.5) 线程命名前缀：便于日志排查
        executor.setThreadNamePrefix("task-query-");
        // 1.6) 拒绝策略：队列满时由调用线程执行，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 1.7) 初始化线程池
        executor.initialize();
        // 1.8) 以 Executor 接口暴露给业务层使用
        return executor;
    }
}
