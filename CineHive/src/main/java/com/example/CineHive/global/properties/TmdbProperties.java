package com.example.CineHive.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.tmdb")
public class TmdbProperties {
    private String baseUrl;   // e.g. https://api.themoviedb.org/3
    private String apiKey;    // v3 방식
    private String v4Token;   // v4 Bearer 방식(선택)
    private Integer pageSize; // 기본 20 (옵션)
}