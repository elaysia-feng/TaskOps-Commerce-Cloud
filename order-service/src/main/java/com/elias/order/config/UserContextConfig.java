package com.elias.order.config;

import com.elias.common.context.UserContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserContextConfig {

    @Bean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter();
    }
}
