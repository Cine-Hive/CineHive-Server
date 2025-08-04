package com.example.CineHive.domain.auth.password.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 찾기 요청 DTO")
public record ForgotPasswordRequest(
        @Schema(description = "가입 시 사용한 이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email
) {}