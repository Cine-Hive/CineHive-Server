package com.example.CineHive.dto.member;

import com.example.CineHive.entity.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 로그인 성공 시 클라이언트에 반환되는 데이터를 담는 DTO입니다.
 * JWT 토큰, 신규 회원 여부, 그리고 기본적인 회원 정보를 포함합니다.
 * record의 기본 생성자를 사용하여 불변성을 보장하고 객체 생성을 명확하게 합니다.
 */
@Schema(description = "로그인 성공 응답 DTO")
public record LoginResponseDto(
        @Schema(description = "JWT 액세스 토큰")
        String token,

        @Schema(description = "신규 회원 여부 (true이면 클라이언트에서 추가 정보 입력 페이지로 유도)")
        boolean isNewMember,

        @Schema(description = "로그인한 회원 정보")
        MemberInfo memberInfo // 필드명을 타입과 일치시켜 명확성 증대
) {
    /**
     * 로그인한 회원의 상세 정보를 담는 내부 DTO입니다.
     * 클라이언트에 노출해도 안전한 정보만 포함합니다.
     */
    @Schema(description = "로그인한 회원의 상세 정보")
    public record MemberInfo(
            @Schema(description = "회원 고유 ID")
            Long id,

            @Schema(description = "이메일")
            String email,

            @Schema(description = "이름")
            String name,

            @Schema(description = "닉네임")
            String nickname,

            @Schema(description = "성별")
            String gender,

            @Schema(description = "선호 장르 목록")
            Set<String> genres
    ) {
        /**
         * Member 엔티티로부터 MemberInfo DTO를 생성하는 정적 팩토리 메서드입니다.
         * 생성자 대신 정적 팩토리 메서드를 사용하여 "변환"의 의도를 명확히 합니다.
         *
         * @param member 원본 Member 엔티티
         * @return 변환된 MemberInfo DTO
         */
        public static MemberInfo from(Member member) {
            return new MemberInfo(
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getNickname(),
                    member.getGender() != null ? member.getGender().name() : null, // Null-safe 처리
                    member.getGenres() // Set<String> 타입이므로 그대로 전달
            );
        }
    }
}