package com.example.CineHive.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth2")
@Getter
@Setter
public class OAuthProperties {

    private final Naver naver = new Naver();
    private final Kakao kakao = new Kakao();
    private final Google google = new Google();

    @Getter
    @Setter
    public static class Naver {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String state;
        private String tokenUri;
        private String userInfoUri;
    }

    @Getter
    @Setter
    public static class Kakao {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
        private String scope;
    }

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope;
        private String tokenUri;
        private String userInfoUri;
    }
}
