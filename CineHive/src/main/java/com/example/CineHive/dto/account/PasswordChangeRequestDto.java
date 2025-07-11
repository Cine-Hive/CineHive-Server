package com.example.CineHive.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequestDto(
        @NotBlank
        String oldPassword,

        @NotBlank @Size(min = 8, max = 20)
        String newPassword
) {}