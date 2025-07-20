package com.example.CineHive.service.member;

import com.example.CineHive.dto.user.LoginRequestDto;
import com.example.CineHive.dto.user.LoginResponseDto;
import com.example.CineHive.dto.user.MemberRegisterRequestDto;
import com.example.CineHive.entity.user.LoginHistory;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.mapper.member.MemberMapper;
import com.example.CineHive.repository.member.LoginHistoryRepository;
import com.example.CineHive.repository.member.MemberRepository;
import com.example.CineHive.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 회원 가입, 로그인, 정보 수정 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(MemberRegisterRequestDto requestDto) {
        log.info("새로운 회원 가입을 시작합니다. 이메일: {}", requestDto.email());
        if (memberRepository.existsByEmail(requestDto.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(requestDto.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        User user = MemberMapper.toEntity(requestDto, passwordEncoder);
        User savedUser = memberRepository.save(user);
        log.info("회원 가입이 완료되었습니다. 회원 ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto, String userAgent) {
        log.info("로그인 시도: {}", requestDto.email());
        User user = memberRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        recordLoginHistory(user, userAgent);

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("로그인 성공: {}", requestDto.email());

        return new LoginResponseDto(
                token,
                false, // 일반 로그인은 항상 기존 회원이므로 false
                LoginResponseDto.MemberInfo.from(user)
        );
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        log.info("비밀번호 변경 시도: {}", email);
        User user = findByEmail(email);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            // NOTE: '기존 비밀번호 불일치'는 로그인 실패와는 성격이 다르므로
            //       'INVALID_CREDENTIALS' 대신 'INVALID_INPUT_VALUE'를 사용합니다.
            //       더 명확한 에러를 위해 'PASSWORD_MISMATCH' 같은 ErrorCode를 추가하는 것도 좋은 방법입니다.
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        user.changePassword(passwordEncoder.encode(newPassword));
        log.info("비밀번호 변경 완료: {}", email);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    @Override
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    @Override
    public User findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void recordLoginHistory(User user, String userAgent) {
        String browser = parseBrowserFromUserAgent(userAgent);
        LoginHistory loginHistory = loginHistoryRepository.findByMember(user)
                .orElseGet(() -> new LoginHistory(null, user, LocalDateTime.now(), null, null));

        loginHistory.updateLoginInfo(browser);
        loginHistoryRepository.save(loginHistory);
        log.debug("로그인 기록 저장 완료. 회원 ID: {}", user.getId());
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
