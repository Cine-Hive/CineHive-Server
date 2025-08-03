package com.example.CineHive.global.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 리뷰 관련 설정을 application.yml에서 읽어오는 클래스입니다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties("app.review")
public class ReviewProperties {
    private int maxPageSize;
    private int defaultPageSize;
}