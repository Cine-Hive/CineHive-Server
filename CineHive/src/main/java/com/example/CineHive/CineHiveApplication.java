package com.example.CineHive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableRedisRepositories
@EnableJpaRepositories(basePackages = "com.example.CineHive.domain")
public class CineHiveApplication {
	public static void main(String[] args) {
		SpringApplication.run(CineHiveApplication.class, args);
	}
}
