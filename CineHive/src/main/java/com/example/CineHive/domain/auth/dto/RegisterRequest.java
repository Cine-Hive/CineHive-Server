package com.example.CineHive.domain.auth.controller.dto;

import com.example.CineHive.global.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

import static com.example.CineHive.global.validation.ValidationPatterns.*;

@Schema(description = "회원가입 요청 DTO")
@PasswordMatches(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
public record RegisterRequest(
        @Schema(description = "이메일", example = "newuser@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8~20자)", example = "newpassword123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = PASSWORD_PATTERN,
                message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8~20자로 작성해야 합니다.")
        String password,

        @Schema(description = "비밀번호 확인", example = "newpassword123!")
        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        String confirmPassword,

        @Schema(description = "사용자 이름 (한글 또는 영문)", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        @Pattern(regexp = NAME_PATTERN, message = "이름은 한글 또는 영문으로만 구성되어야 합니다.")
        String name,

        @Schema(description = "닉네임 (한글, 영문, 숫자 조합 2~10자)", example = "길동이123")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 작성해야 합니다.")
        @Pattern(regexp = NICKNAME_PATTERN, message = "닉네임은 공백이나 특수문자를 포함할 수 없습니다.")
        String nickname,

        @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
        @NotBlank(message = "성별은 필수입니다.")
        @Pattern(regexp = "MALE|FEMALE|OTHER", message = "성별은 MALE, FEMALE, OTHER 중 하나여야 합니다.")
        String gender,

        @Schema(description = "선호 장르 목록", example = "[\"ACTION\", \"COMEDY\"]")
        List<String> genres
) {}