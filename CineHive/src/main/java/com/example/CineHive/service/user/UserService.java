package com.example.CineHive.service.user;

import com.example.CineHive.dto.user.LoginHistoryDto;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.user.LoginHistory;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.mapper.LoginHistoryMapper;
import com.example.CineHive.mapper.UserMapper;
import com.example.CineHive.repository.user.LoginHistoryRepository;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.util.JwtUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.time.LocalDateTime;
import java.util.Optional;
@Service
public class UserService{
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public String generateJwtToken(String email) {
        return jwtUtil.generateToken(email);
    }

    private String getBrowserInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        if (StringUtils.isEmpty(userAgent)) {
            return "Unknown";
        }

        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            return "Chrome";
        } else if (userAgent.contains("Edg")) {
            return "Edge";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "Internet Explorer";
        } else {
            return "Other";
        }
    }
    @Transactional
    public boolean registerUser(UserDto userDto) {
        UserMapper userMapper = new UserMapper();
        User user = userMapper.toEntity(userDto);
        user.setMemPw(passwordEncoder.encode(userDto.getMemPassword())); // 비밀번호 암호화

        userRepository.save(user);
        return true;
    }


    @Transactional
    public LoginHistoryDto loginUser(String memEmail, String memPassword, HttpServletRequest request) {
        Optional<User> existingUser = userRepository.findByMemEmail(memEmail);

        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        User user = existingUser.get();

        if (!passwordEncoder.matches(memPassword, user.getMemPw())) {
            throw new IllegalArgumentException("비밀번호가 맞지 않습니다.");
        }

        // 브라우저 정보 가져오기
        String browser = getBrowserInfo(request);

        // 로그인 기록 조회
        Optional<LoginHistory> loginHistoryOpt = loginHistoryRepository.findByUser(user);
        LocalDateTime now = LocalDateTime.now();
        LoginHistory loginHistory;

        if (loginHistoryOpt.isEmpty()) {
            // 최초 로그인 기록 생성
            loginHistory = new LoginHistory(null, user, now, now, browser);
        } else {
            // 기존 로그인 기록 업데이트
            loginHistory = loginHistoryOpt.get();
            loginHistory.setLastLoginDate(now);
            loginHistory.setBrowser(browser);
        }

        loginHistoryRepository.save(loginHistory);

        // 엔티티를 DTO로 변환하여 반환
        return LoginHistoryMapper.toDto(loginHistory);
    }

    public boolean checkUserExists(String memEmail) {
        return userRepository.findByMemEmail(memEmail).isPresent();
    }
    public User getUserInfo(String memEmail) {
        return userRepository.findByMemEmail(memEmail).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

}