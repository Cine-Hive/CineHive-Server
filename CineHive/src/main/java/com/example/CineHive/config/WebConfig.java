package com.example.CineHive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 관련 추가 설정을 위한 클래스입니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Bean Validation을 위한 Validator 빈 등록
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}