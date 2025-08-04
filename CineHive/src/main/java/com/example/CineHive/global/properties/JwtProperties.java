package com.example.CineHive.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties("app.jwt")
public class JwtProperties {
    private String secretKey;
    private Duration accessTokenExpiration;
    private Duration refreshTokenExpiration;
}
