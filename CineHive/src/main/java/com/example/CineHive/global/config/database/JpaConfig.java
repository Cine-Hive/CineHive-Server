package com.example.CineHive.global.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "com.example.CineHive.repository"
})
public class JpaConfig {
    // 기존 JPA 설정 유지
} 