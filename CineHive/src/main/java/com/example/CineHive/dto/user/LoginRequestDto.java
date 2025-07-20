package com.example.CineHive.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public record LoginRequestDto(
        @Schema(description = "사용자 이메일", example = "test@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email
        String email,

        @Schema(description = "사용자 비밀번호", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {}