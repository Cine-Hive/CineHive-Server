package com.example.CineHive.service.member;

import com.example.CineHive.dto.member.LoginRequestDto;
import com.example.CineHive.dto.member.MemberRegisterRequestDto;
import com.example.CineHive.dto.member.LoginResponseDto;
import com.example.CineHive.entity.member.LoginHistory;
import com.example.CineHive.entity.member.Member;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 새로운 회원을 등록합니다. 이메일과 닉네임의 중복을 확인합니다.
     * @param requestDto 회원가입에 필요한 정보 (이메일, 비밀번호, 닉네임 등)
     * @return 생성된 Member 엔티티
     * @throws IllegalArgumentException 이메일 또는 닉네임이 이미 존재할 경우 발생
     */
    @Transactional
    public Member register(MemberRegisterRequestDto requestDto) {
        log.info("Registering new member with email: {}", requestDto.email());
        if (memberRepository.existsByEmail(requestDto.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByNickname(requestDto.nickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Member member = MemberMapper.toEntity(requestDto, passwordEncoder);
        return memberRepository.save(member);
    }

    /**
     * 사용자의 로그인을 처리하고, 성공 시 JWT 토큰과 회원 정보를 반환합니다.
     * @param requestDto 로그인 정보 (이메일, 비밀번호)
     * @return JWT 토큰과 회원 정보를 포함한 LoginResponseDto
     * @throws IllegalArgumentException 이메일이 존재하지 않거나 비밀번호가 일치하지 않을 경우 발생
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto, String userAgent) {
        log.info("Attempting login for email: {}", requestDto.email());
        Member member = memberRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(requestDto.password(), member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 로그인 성공 후 부가 작업 (로그 기록)
        recordLoginHistory(member, userAgent);

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getEmail());
        log.info("Login successful for email: {}", requestDto.email());

        return MemberMapper.toLoginResponseDto(member, token);
    }

    /**
     * 비밀번호를 변경합니다.
     * @param email 사용자 이메일
     * @param oldPassword 기존 비밀번호
     * @param newPassword 새 비밀번호
     * @throws IllegalArgumentException 사용자를 찾을 수 없거나 기존 비밀번호가 틀린 경우
     */
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        log.info("Changing password for email: {}", email);
        Member member = findByEmail(email);
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }
        member.changePassword(passwordEncoder.encode(newPassword));
        // @Transactional에 의해 변경 감지(Dirty Checking)되어 자동 save
    }

    /**
     * 이메일 사용 가능 여부를 확인합니다.
     * @param email 확인할 이메일
     * @return 사용 가능하면 true, 아니면 false
     */
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임 사용 가능 여부를 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 사용 가능하면 true, 아니면 false
     */
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    /**
     * 이메일로 회원을 조회합니다.
     * @param email 조회할 이메일
     * @return Member 엔티티
     * @throws IllegalArgumentException 해당 이메일의 회원이 없을 경우
     */
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 로그인 기록을 저장하거나 업데이트합니다.
     * @param member 로그인한 회원 엔티티
     * @param userAgent 사용자의 User-Agent 문자열
     */
    private void recordLoginHistory(Member member, String userAgent) {
        String browser = parseBrowserFromUserAgent(userAgent);
        LoginHistory loginHistory = loginHistoryRepository.findByMember(member)
                .orElse(new LoginHistory(null, member, LocalDateTime.now(), null, null));

        loginHistory.updateLoginInfo(browser);
        loginHistoryRepository.save(loginHistory);
        log.debug("Recorded login history for member id: {}", member.getId());
    }

    /**
     * User-Agent 문자열에서 브라우저 정보를 추출합니다.
     * @param userAgent HTTP 요청의 User-Agent 헤더 값
     * @return 브라우저 이름 (e.g., "Chrome", "Edge", "Unknown")
     */
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