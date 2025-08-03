package com.example.CineHive.domain.user.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static com.example.CineHive.global.validation.ValidationPatterns.PASSWORD_PATTERN;

@Schema(description = "비밀번호 변경 요청 DTO")
public record UpdatePasswordRequest(
        @Schema(description = "기존 비밀번호", example = "password123!")
        @NotBlank(message = "기존 비밀번호는 필수입니다.")
        String oldPassword,

        @Schema(description = "새로운 비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "newPassword456!")
        @NotBlank(message = "새로운 비밀번호는 필수입니다.")
        @Pattern(regexp = PASSWORD_PATTERN,
                message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 작성해야 합니다.")
        String newPassword
) {}