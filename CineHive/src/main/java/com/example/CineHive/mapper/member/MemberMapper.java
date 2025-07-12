package com.example.CineHive.mapper.member;

import com.example.CineHive.dto.member.MemberRegisterRequestDto;
import com.example.CineHive.dto.member.LoginResponseDto;
import com.example.CineHive.entity.member.Gender;
import com.example.CineHive.entity.member.Member;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

public final class MemberMapper {

    private MemberMapper() {} // 유틸리티 클래스 인스턴스화 방지

    public static Member toEntity(MemberRegisterRequestDto dto, PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .name(dto.name())
                .nickname(dto.nickname())
                .gender(Gender.valueOf(dto.gender().toUpperCase()))
                .genres(new HashSet<>(dto.genres()))
                .build();
    }

    public static LoginResponseDto toLoginResponseDto(Member member, String token) {
        LoginResponseDto.MemberInfo memberInfo = new LoginResponseDto.MemberInfo(member);

        return LoginResponseDto.builder()
                .token(token)
                .isNewMember(false) // 일반 로그인이므로 항상 기존 회원
                .member(memberInfo)
                .build();
    }
}