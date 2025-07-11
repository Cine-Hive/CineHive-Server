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
                .password(passwordEncoder.encode(dto.password())) // 서비스에서 암호화
                .name(dto.name())
                .nickname(dto.nickname())
                .gender(Gender.valueOf(dto.gender().toUpperCase()))
                .genres(new HashSet<>(dto.genres())) // List를 Set으로 변환
                .build();
    }

    public static LoginResponseDto toLoginResponseDto(Member member, String token) {
        LoginResponseDto.MemberInfo memberInfo = LoginResponseDto.MemberInfo.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .gender(member.getGender().name())
                .genres(member.getGenres().stream().toList()) // Set을 List로 변환
                .build();

        return LoginResponseDto.builder()
                .token(token)
                .member(memberInfo)
                .build();
    }
}