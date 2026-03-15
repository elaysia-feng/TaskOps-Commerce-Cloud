package com.elias.aiproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.elias.aiproxy", "com.elias.common"})
public class AiProxyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiProxyServiceApplication.class, args);
    }
}