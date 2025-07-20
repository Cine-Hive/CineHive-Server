package com.example.CineHive.service.member;

import com.example.CineHive.dto.auth.LoginRequestDto;
import com.example.CineHive.dto.auth.LoginResponseDto;
import com.example.CineHive.dto.auth.RegisterRequestDto;
import com.example.CineHive.entity.user.User;

/**
 * 회원 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.
 */
public interface MemberService {

    /**
     * 새로운 회원을 등록합니다.
     * @param requestDto 회원가입 정보 DTO
     * @return 생성된 Member 엔티티
     */
    User register(RegisterRequestDto requestDto);

    /**
     * 일반 로그인을 처리하고 JWT 토큰을 발급합니다.
     * @param requestDto 로그인 정보 DTO
     * @param userAgent  클라이언트의 User-Agent 정보
     * @return 로그인 응답 DTO (토큰 및 회원 정보 포함)
     */
    LoginResponseDto login(LoginRequestDto requestDto, String userAgent);

    /**
     * 회원의 비밀번호를 변경합니다.
     * @param email       회원 이메일 (인증된 사용자 정보에서 추출)
     * @param oldPassword 기존 비밀번호
     * @param newPassword 새로운 비밀번호
     */
    void changePassword(String email, String oldPassword, String newPassword);

    /**
     * 이메일 사용 가능 여부를 확인합니다.
     * @param email 확인할 이메일
     * @return 사용 가능 시 true
     */
    boolean isEmailAvailable(String email);

    /**
     * 닉네임 사용 가능 여부를 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 사용 가능 시 true
     */
    boolean isNicknameAvailable(String nickname);

    /**
     * 이메일로 회원을 조회합니다.
     * @param email 조회할 이메일
     * @return 조회된 Member 엔티티
     */
    User findByEmail(String email);
}