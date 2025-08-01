package com.example.CineHive.global.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityPolicyProperties {

    private final Login login = new Login();
    private final Password password = new Password();
    private final RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class Login {
        private int maxAttempts = 5;
        private Duration lockoutDuration = Duration.ofMinutes(15);
        private Duration attemptWindow = Duration.ofHours(1);
    }

    @Getter
    @Setter
    public static class Password {
        private int historySize = 3;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private final ForgotPassword forgotPassword = new ForgotPassword();

        @Getter
        @Setter
        public static class ForgotPassword {
            private Duration emailWindow = Duration.ofMinutes(1);
            private int emailMaxRequests = 2;
            private int ipMaxRequests = 20;
            private Duration ipWindow = Duration.ofHours(1);
        }
    }
}