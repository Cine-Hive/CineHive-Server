package com.example.CineHive.global.config.web;

import com.example.CineHive.global.properties.TmdbProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient tmdbRestClient(RestClient.Builder builder, TmdbProperties props) {
        RestClient.Builder b = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.hasText(props.getV4Token())) {
            b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getV4Token());
        }
        return b.build();
    }
}