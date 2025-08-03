package com.example.CineHive.global.config.init;

import com.example.CineHive.domain.user.Gender;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.domain.user.UserRole;
import com.example.CineHive.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 관리자 계정을 초기화하는 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("관리자")
                    .nickname("관리자")
                    .role(UserRole.ROLE_ADMIN)
                    .gender(Gender.OTHER)
                    .build();
            userRepository.save(admin);
        }
    }
}