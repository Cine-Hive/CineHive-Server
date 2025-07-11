package com.example.CineHive.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameChangeRequestDto(
        @NotBlank @Size(min = 2, max = 10)
        String nickname
) {}