package com.example.CineHive.dto.global;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * API 에러 발생 시 응답 본문에 포함될 상세 정보를 담는 레코드입니다.
 */
@Schema(description = "API 에러 응답 상세 정보")
public record ErrorResponse(
        @Schema(description = "에러 발생 시각")
        LocalDateTime timestamp,
        @Schema(description = "HTTP 상태 코드")
        int status,
        @Schema(description = "에러 이름 (예: Not Found)")
        String error,
        @Schema(description = "에러 메시지")
        String message,
        @Schema(description = "요청 경로")
        String path
) {
    /**
     * 에러 정보를 바탕으로 ErrorResponse 객체를 생성합니다.
     * @param status HTTP 상태 코드
     * @param error 에러 이름
     * @param message 에러 메시지
     * @param path 요청 경로
     * @return 생성된 ErrorResponse 객체
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path);
    }
}