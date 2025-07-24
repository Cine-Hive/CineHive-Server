package com.example.CineHive.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청 DTO")
public record UpdatePasswordRequest(
        @Schema(description = "기존 비밀번호", example = "password123")
        @NotBlank
        String oldPassword,

        @Schema(description = "새로운 비밀번호", example = "newPassword456")
        @NotBlank @Size(min = 8, max = 20)
        String newPassword
) {}