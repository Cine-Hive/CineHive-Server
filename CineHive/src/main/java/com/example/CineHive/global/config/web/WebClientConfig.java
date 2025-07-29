package com.example.CineHive.global.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 애플리케이션 전역에서 사용할 WebClient Bean을 설정하는 클래스입니다.
 */
@Configuration
public class WebClientConfig {

    /**
     * 기본 설정을 가진 WebClient Bean을 생성하여 Spring 컨테이너에 등록합니다.
     * 이제 다른 컴포넌트에서 @Autowired 또는 생성자 주입을 통해 WebClient를 사용할 수 있습니다.
     * @return WebClient 인스턴스
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}