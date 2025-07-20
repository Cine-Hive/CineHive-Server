package com.example.CineHive.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 변경 요청 DTO")
public record UpdateNicknameRequest(
        @Schema(description = "새로운 닉네임", example = "새로운길동이")
        @NotBlank @Size(min = 2, max = 10)
        String nickname
) {}