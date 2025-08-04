package com.example.CineHive.domain.auth.password.dto;

import com.example.CineHive.global.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static com.example.CineHive.global.validation.ValidationPatterns.PASSWORD_PATTERN;

@Schema(description = "비밀번호 재설정 요청 DTO")
@PasswordMatches(
        passwordField = "newPassword",
        confirmPasswordField = "confirmPassword",
        message = "새로운 비밀번호와 비밀번호 확인이 일치하지 않습니다."
)
public record ResetPasswordRequest(
        @Schema(description = "이메일로 받은 URL의 selector 값", example = "selector-string")
        @NotBlank(message = "Selector는 필수입니다.")
        String selector,

        @Schema(description = "이메일로 받은 URL의 validator 값", example = "validator-string")
        @NotBlank(message = "Validator는 필수입니다.")
        String validator,

        @Schema(description = "새로운 비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "newPassword123!")
        @NotBlank(message = "새로운 비밀번호는 필수입니다.")
        @Pattern(regexp = PASSWORD_PATTERN,
                message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 작성해야 합니다.")
        String newPassword,

        @Schema(description = "새로운 비밀번호 확인", example = "newPassword123!")
        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        String confirmPassword
) {}