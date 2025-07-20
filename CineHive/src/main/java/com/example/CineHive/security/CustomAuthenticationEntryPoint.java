package com.example.CineHive.security;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 호출되는 핸들러입니다.
 * Spring Security의 예외 처리 과정에서 사용되며, 401 Unauthorized 응답을 반환합니다.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증되지 않은 사용자의 요청을 처리하여 표준화된 에러 응답을 반환합니다.
     *
     * @param request       요청 객체
     * @param response      응답 객체
     * @param authException Spring Security에 의해 발생한 인증 예외
     * @throws IOException 응답 작성 중 I/O 오류 발생 시
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // ErrorCode를 사용하여 일관된 에러 메시지를 생성합니다.
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED_ACCESS;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorCode.getStatus().value(),
                errorCode.getMessage()
        );

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
