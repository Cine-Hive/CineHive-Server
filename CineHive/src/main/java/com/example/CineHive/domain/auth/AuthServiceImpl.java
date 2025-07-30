package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.auth.dto.*;
import com.example.CineHive.domain.common.DomainFinder;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.user.UserRepository;
import com.example.CineHive.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final DomainFinder domainFinder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration.refresh-token}")
    private String refreshTokenExpirationIso; // e.g., "P30D"

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        log.info("새로운 사용자 가입을 시작합니다. 이메일: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        User user = User.from(request, passwordEncoder);
        userRepository.save(user);
        log.info("사용자 가입이 완료되었습니다. 이메일: {}", request.email());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String userAgent) {
        log.info("로그인 시도: {}", request.email());
        User user = domainFinder.findUserByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // User 엔티티를 통해 로그인 기록 업데이트 (1:1 양방향 매핑 활용)
        String browser = parseBrowserFromUserAgent(userAgent);
        user.updateLoginHistory(browser);
        log.debug("로그인 기록 업데이트. 사용자 ID: {}", user.getId());

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getEmail());

        // RefreshToken 만료 단위를 ISO-8601 Duration 형식으로 처리
        long refreshTokenValidityInSeconds = Duration.parse(refreshTokenExpirationIso).toSeconds();
        refreshTokenRepository.save(new RefreshToken(user.getEmail(), refreshTokenValue, refreshTokenValidityInSeconds));
        log.info("Refresh Token이 Redis에 저장되었습니다. User: {}, 만료 시간(초): {}", user.getEmail(), refreshTokenValidityInSeconds);

        log.info("로그인 성공: {}", request.email());

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