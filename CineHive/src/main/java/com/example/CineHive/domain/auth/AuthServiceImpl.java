package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.auth.dto.*;
import com.example.CineHive.domain.common.DomainFinder;
import com.example.CineHive.domain.mail.EmailService;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.domain.user.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.jwt.JwtTokenProvider;
import com.example.CineHive.global.config.security.SecurityPolicyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class AuthServiceImpl implements AuthService {

    // Core Dependencies
    private final DomainFinder domainFinder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Token & Session Dependencies
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // External Service Dependencies
    private final EmailService emailService;

    // Configuration Properties
    private final SecurityPolicyProperties securityPolicy;
    @Value("${app.jwt.refresh-token-expiration}")
    String refreshTokenExpirationIso;

    // Constants
    private static final String LOGIN_ATTEMPT_KEY_PREFIX = "login:attempts:";
    private static final String ACCOUNT_LOCK_KEY_PREFIX = "account:lock:";
    private static final String FORGOT_PASSWORD_EMAIL_KEY_PREFIX = "rate-limit:forgot-password:email:";
    private static final String FORGOT_PASSWORD_IP_KEY_PREFIX = "rate-limit:forgot-password:ip:";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

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

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
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
        User user = domainFinder.findUserByEmail(userEmail);
        refreshTokenRepository.deleteById(user.getEmail());
        log.info("로그아웃 처리 완료. Redis의 Refresh Token 삭제. User: {}", user.getEmail());
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public void createPasswordResetToken(ForgotPasswordRequest request, String clientIp) {
        validateRateLimit(request.email(), clientIp);

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String selector = generateRandomBase64(16);
            String validator = generateRandomBase64(32);
            String validatorHash = passwordEncoder.encode(validator);
            Instant expiryDate = Instant.now().plus(30, ChronoUnit.MINUTES);

            PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(user)
                    .orElse(null);

            if (resetToken != null) {
                log.debug("기존 비밀번호 재설정 토큰을 갱신합니다. 사용자: {}", user.getEmail());
                resetToken.updateToken(selector, validatorHash, expiryDate);
            } else {
                log.debug("새로운 비밀번호 재설정 토큰을 생성합니다. 사용자: {}", user.getEmail());
                resetToken = new PasswordResetToken(user, selector, validatorHash, expiryDate);
            }

            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), selector, validator);
            log.info("비밀번호 재설정 토큰이 생성되어 이메일로 발송되었습니다. 사용자: {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findBySelector(request.selector())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BusinessException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        if (!passwordEncoder.matches(request.validator(), resetToken.getValidatorHash())) {
            throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
        }

        User user = resetToken.getUser();
        validatePasswordHistory(user, request.newPassword());
        archiveOldPassword(user);

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        log.info("비밀번호가 성공적으로 재설정되었습니다. 사용자: {}", user.getEmail());
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

    /**
     * 비밀번호 찾기 요청에 대한 Rate Limit을 검증합니다.
     * @param email 요청 이메일
     * @param clientIp 요청 IP
     */
    private void validateRateLimit(String email, String clientIp) {
        String emailKey = FORGOT_PASSWORD_EMAIL_KEY_PREFIX + email;
        String ipKey = FORGOT_PASSWORD_IP_KEY_PREFIX + clientIp;

        BoundValueOperations<String, String> emailOps = redisTemplate.boundValueOps(emailKey);
        Long emailRequests = emailOps.increment();
        if (emailRequests != null && emailRequests == 1) {
            emailOps.expire(securityPolicy.getRateLimit().getForgotPassword().getEmailWindow());
        }
        if (emailRequests != null && emailRequests > securityPolicy.getRateLimit().getForgotPassword().getEmailMaxRequests()) {
            log.warn("비밀번호 재설정 요청 횟수 초과 (이메일 기준): {}", email);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        BoundValueOperations<String, String> ipOps = redisTemplate.boundValueOps(ipKey);
        Long ipRequests = ipOps.increment();
        if (ipRequests != null && ipRequests == 1) {
            ipOps.expire(securityPolicy.getRateLimit().getForgotPassword().getIpWindow());
        }
        if (ipRequests != null && ipRequests > securityPolicy.getRateLimit().getForgotPassword().getIpMaxRequests()) {
            log.warn("비밀번호 재설정 요청 횟수 초과 (IP 기준): {}", clientIp);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
    }

    private void validatePasswordHistory(User user, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        history.stream()
                .limit(securityPolicy.getPassword().getHistorySize())
                .forEach(record -> {
                    if (passwordEncoder.matches(newPassword, record.getPasswordHash())) {
                        throw new BusinessException(ErrorCode.PASSWORD_REUSE_PROHIBITED);
                    }
                });
    }

    private void archiveOldPassword(User user) {
        PasswordHistory newHistoryRecord = new PasswordHistory(user, user.getPassword());
        passwordHistoryRepository.save(newHistoryRecord);

        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        if (history.size() > securityPolicy.getPassword().getHistorySize()) {
            PasswordHistory oldestRecord = history.get(history.size() - 1);
            passwordHistoryRepository.delete(oldestRecord);
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

    private String generateRandomBase64(int byteLength) {
        byte[] randomBytes = new byte[byteLength];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}