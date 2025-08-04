package com.example.CineHive.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder(RestTemplateBuilder restTemplateBuilder) {
        return RestClient.builder(restTemplateBuilder.build());
    }
}