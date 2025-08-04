package com.example.CineHive.global.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties("app.security.password.reset")
public class PasswordResetProperties {
    private Duration tokenExpiry;
}
