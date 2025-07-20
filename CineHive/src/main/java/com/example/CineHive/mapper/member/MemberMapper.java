package com.example.CineHive.mapper.member;

import com.example.CineHive.dto.user.MemberRegisterRequestDto;
import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.entity.user.Gender;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.user.ProviderType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Collectors;

/**
 * 회원(Member) 관련 매퍼 클래스입니다.
 * 이 클래스는 오직 요청 DTO(Request DTO)를 엔티티(Entity)로 변환하는 책임만 가집니다.
 */
public final class MemberMapper {

    /**
     * 유틸리티 클래스는 인스턴스화할 수 없습니다.
     */
    private MemberMapper() {
        throw new IllegalStateException("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * 일반 회원가입 요청 DTO를 Member 엔티티로 변환합니다.
     * 이 과정에서 비밀번호는 안전하게 암호화되며, 프로바이더는 'LOCAL'로 설정됩니다.
     *
     * @param dto             회원가입 요청 데이터
     * @param passwordEncoder 비밀번호 암호화기
     * @return 생성된 Member 엔티티
     */
    public static User toEntity(MemberRegisterRequestDto dto, PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .name(dto.name())
                .nickname(dto.nickname())
                .gender(Gender.valueOf(dto.gender().toUpperCase()))
                .genres(dto.genres().stream()
                        .map(Genre::valueOf)
                        .collect(Collectors.toSet()))
                .provider(ProviderType.LOCAL)
                .build();
    }
}