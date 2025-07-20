package com.example.CineHive.security;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.ErrorResponse;
import com.example.CineHive.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근 시 호출되는 핸들러입니다.
 * 401 Unauthorized 응답을 반환합니다.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED_ACCESS;
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(errorResponse);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}