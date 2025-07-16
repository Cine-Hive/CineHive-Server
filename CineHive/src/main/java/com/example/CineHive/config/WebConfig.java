package com.example.CineHive.config;

import com.example.CineHive.config.converter.StringToEnumConverterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 관련 추가 설정을 위한 클래스.
 * (컨버터, 포맷터, 인터셉터, CORS 전역 설정 등)
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringToEnumConverterFactory stringToEnumConverterFactory;

    /**
     * Spring의 FormatterRegistry에 커스텀 컨버터를 등록합니다.
     * 여기에 등록된 컨버터는 @RequestParam, @PathVariable 등의 변환에 사용됩니다.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(stringToEnumConverterFactory);
    }

    /**
     * Bean Validation을 위한 Validator 빈 등록
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}