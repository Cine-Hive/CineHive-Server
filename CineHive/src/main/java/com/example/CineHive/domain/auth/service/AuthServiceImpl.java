package com.example.CineHive.domain.auth.service;

import com.example.CineHive.domain.auth.dto.*;
import com.example.CineHive.domain.auth.entity.RefreshToken;
import com.example.CineHive.domain.auth.repository.RefreshTokenRepository;
import com.example.CineHive.domain.mail.service.EmailService;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.properties.SecurityPolicyProperties;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    // Core Dependencies
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Token & Session Dependencies
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // External Service Dependencies
    private final EmailService emailService;

    // Configuration Properties
    private final SecurityPolicyProperties securityPolicy;
    @Value("${app.jwt.refresh-token-expiration}")
    private String refreshTokenExpirationIso;

    // Constants
    private static final String LOGIN_ATTEMPT_KEY_PREFIX = "login:attempts:";
    private static final String ACCOUNT_LOCK_KEY_PREFIX = "account:lock:";

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        log.info("새로운 사용자 가입을 시작합니다. 이메일: {}", request.email());
        validateRegistrationRules(request);

        User user = User.from(request, passwordEncoder);
        userRepository.save(user);
        log.info("사용자 가입이 완료되었습니다. 이메일: {}", request.email());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String userAgent) {
        String email = request.email();
        String lockKey = ACCOUNT_LOCK_KEY_PREFIX + email;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            handleLoginFailure(email);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        handleLoginSuccess(email);

        String browser = parseBrowserFromUserAgent(userAgent);
        user.updateLoginHistory(browser);
        log.debug("로그인 기록 업데이트. 사용자 ID: {}", user.getId());

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getEmail());

        long refreshTokenValidityInSeconds = Duration.parse(refreshTokenExpirationIso).toSeconds();
        refreshTokenRepository.save(new RefreshToken(user.getEmail(), refreshTokenValue, refreshTokenValidityInSeconds));
        log.info("Refresh Token이 Redis에 저장되었습니다. User: {}", user.getEmail());

        log.info("로그인 성공: {}", email);
        return new LoginResponse(accessToken, refreshTokenValue, false, LoginResponse.UserInfo.from(user));
    }

    @Override
    @Transactional
    public ReissueTokenResponse reissueToken(ReissueTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        String email = jwtTokenProvider.extractUsername(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS));

        if (!storedToken.getToken().equals(refreshToken)) {
            refreshTokenRepository.deleteById(email);
            log.warn("Refresh Token 불일치 감지 (탈취 의심). 강제 로그아웃 처리: {}", email);
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        long refreshTokenValidityInSeconds = Duration.parse(refreshTokenExpirationIso).toSeconds();
        refreshTokenRepository.save(new RefreshToken(email, newRefreshToken, refreshTokenValidityInSeconds));
        log.info("토큰 재발급 및 로테이션 완료. User: {}", email);

        return new ReissueTokenResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String userEmail) {
        refreshTokenRepository.deleteById(userEmail);
        log.info("로그아웃 처리 완료. Redis의 Refresh Token 삭제. User: {}", userEmail);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    private void validateRegistrationRules(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    private void handleLoginSuccess(String email) {
        String attemptKey = LOGIN_ATTEMPT_KEY_PREFIX + email;
        redisTemplate.delete(attemptKey);
    }

    private void handleLoginFailure(String email) {
        String attemptKey = LOGIN_ATTEMPT_KEY_PREFIX + email;
        BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(attemptKey);
        Long attempts = ops.increment();

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, securityPolicy.getLogin().getAttemptWindow());
        }

        if (attempts != null && attempts >= securityPolicy.getLogin().getMaxAttempts()) {
            String lockKey = ACCOUNT_LOCK_KEY_PREFIX + email;
            redisTemplate.opsForValue().set(lockKey, "locked", securityPolicy.getLogin().getLockoutDuration());
            redisTemplate.delete(attemptKey);

            log.warn("계정 잠금 처리됨: {}. {} 동안 잠금.", email, securityPolicy.getLogin().getLockoutDuration());
            emailService.sendAccountLockoutEmail(email);
        }
    }

    private String parseBrowserFromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "Unknown";
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) return "Chrome";
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "Internet Explorer";
        return "Other";
    }
}
