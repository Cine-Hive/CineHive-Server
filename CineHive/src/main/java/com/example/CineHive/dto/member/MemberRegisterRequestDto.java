package com.example.CineHive.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "회원가입 요청 DTO")
public record MemberRegisterRequestDto(
        @Schema(description = "이메일", example = "newuser@example.com")
        @NotBlank @Email
        String email,

        @Schema(description = "비밀번호 (8~20자)", example = "newpassword123")
        @NotBlank @Size(min = 8, max = 20)
        String password,

        @Schema(description = "사용자 이름", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "닉네임 (2~10자)", example = "길동이")
        @NotBlank @Size(min = 2, max = 10)
        String nickname,

        @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
        @NotBlank @Pattern(regexp = "MALE|FEMALE|OTHER")
        String gender,

        @Schema(description = "선호 장르 목록", example = "[\"액션\", \"코미디\"]")
        List<String> genres
) {}