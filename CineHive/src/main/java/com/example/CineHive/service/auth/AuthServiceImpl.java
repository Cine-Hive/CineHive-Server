package com.example.CineHive.service.auth;

import com.example.CineHive.dto.auth.LoginRequest;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.auth.RegisterRequest;
import com.example.CineHive.entity.user.LoginHistory;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.mapper.user.UserMapper;
import com.example.CineHive.repository.user.LoginHistoryRepository;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

        User user = UserMapper.toEntity(request, passwordEncoder);
        userRepository.save(user);
        log.info("사용자 가입이 완료되었습니다. 이메일: {}", request.email());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String userAgent) {
        log.info("로그인 시도: {}", request.email());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        recordLoginHistory(user, userAgent);

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("로그인 성공: {}", request.email());

        return new LoginResponse(token, false, LoginResponse.UserInfo.from(user));
    }

    private void recordLoginHistory(User user, String userAgent) {
        String browser = parseBrowserFromUserAgent(userAgent);
        LoginHistory loginHistory = loginHistoryRepository.findByUser(user)
                .orElseGet(() -> LoginHistory.builder().user(user).browser(browser).build());

        loginHistory.updateLoginInfo(browser);
        loginHistoryRepository.save(loginHistory);
        log.debug("로그인 기록 저장 완료. 사용자 ID: {}", user.getId());
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