package com.example.CineHive.dto.member;

import com.example.CineHive.entity.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

/**
 * 로그인 성공 시 클라이언트에 반환되는 데이터를 담는 DTO입니다.
 * JWT 토큰, 신규 회원 여부, 그리고 기본적인 회원 정보를 포함합니다.
 */
@Schema(description = "로그인 성공 응답 DTO")
@Builder
public record LoginResponseDto(
        @Schema(description = "JWT 액세스 토큰")
        String token,

        @Schema(description = "신규 회원 여부 (true이면 클라이언트에서 추가 정보 입력 페이지로 유도)")
        boolean isNewMember,

        @Schema(description = "로그인한 회원 정보")
        MemberInfo member
) {
    /**
     * 로그인한 회원의 상세 정보를 담는 내부 DTO입니다.
     * 클라이언트에 노출해도 안전한 정보만 포함합니다.
     */
    @Schema(description = "로그인한 회원의 상세 정보")
    @Builder
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
            Set<String> genres // Set<String> 타입으로 유지
    ) {
        /**
         * Member 엔티티로부터 MemberInfo DTO를 생성하는 편리한 생성자입니다.
         * 이 생성자는 Mapper 또는 Service 계층에서 사용됩니다.
         * @param member 원본 Member 엔티티
         */
        public MemberInfo(Member member) {
            this( // record의 기본 생성자 호출
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getNickname(),
                    member.getGender().name(), // Enum을 String으로 변환
                    member.getGenres()       // Set을 그대로 전달
            );
        }
    }
}