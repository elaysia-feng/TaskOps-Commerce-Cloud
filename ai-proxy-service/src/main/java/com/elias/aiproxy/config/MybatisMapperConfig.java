package com.elias.aiproxy.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.elias.aiproxy.mapper")
public class MybatisMapperConfig {
}
