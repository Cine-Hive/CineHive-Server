package com.example.CineHive.domain.auth.password.controller;

import com.example.CineHive.domain.auth.password.dto.ForgotPasswordRequest;
import com.example.CineHive.domain.auth.password.dto.ResetPasswordRequest;
import com.example.CineHive.domain.auth.password.service.PasswordResetService;
import com.example.CineHive.global.dto.MessageResponse;
import com.example.CineHive.global.util.ClientIpExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Password Reset Controller", description = "비밀번호 찾기 및 재설정 API")
@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 찾기 요청 (재설정 이메일 발송)")
    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpServletRequest) {
        String clientIp = ClientIpExtractor.getClientIp(httpServletRequest);
        passwordResetService.createPasswordResetToken(request, clientIp);
    }

    @Operation(summary = "비밀번호 재설정")
    @PostMapping("/reset")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return new MessageResponse("비밀번호가 성공적으로 재설정되었습니다.");
    }
}
