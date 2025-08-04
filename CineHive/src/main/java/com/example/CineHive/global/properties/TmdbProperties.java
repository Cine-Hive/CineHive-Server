package com.example.CineHive.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("app.tmdb")
public class TmdbProperties {
    private String apiKey;
    private String baseUrl;
}
