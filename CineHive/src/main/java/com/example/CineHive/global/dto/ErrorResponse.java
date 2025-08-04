package com.example.CineHive.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "API 에러 응답 상세 정보")
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String error,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<FieldErrorDetail> details
) {
    // 일반 에러용
    public static ErrorResponse of(int status, String code, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, code, error, message, path, List.of());
    }

    // 유효성 검증 에러용
    public static ErrorResponse of(int status, String code, String error, String message, String path, List<FieldErrorDetail> details) {
        return new ErrorResponse(LocalDateTime.now(), status, code, error, message, path, details);
    }
}