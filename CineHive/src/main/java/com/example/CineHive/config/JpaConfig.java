package com.example.CineHive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "com.example.CineHive.repository",
    "com.example.CineHive.repository.media"
})
public class JpaConfig {
    // 기존 JPA 설정 유지
} 