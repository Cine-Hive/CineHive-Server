package com.example.CineHive.dto.global; // 패키지 이동

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 모든 API 응답을 위한 표준 래퍼 클래스입니다.
 */
@Getter
public class ApiResponse<T> {

    private final boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final ErrorResponse error;

    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /**
     * 성공적인 API 응답을 생성합니다.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 실패 API 응답을 생성합니다.
     * ErrorResponse 객체를 직접 받아서 실패 응답을 구성합니다.
     */
    public static <T> ApiResponse<T> error(ErrorResponse errorResponse) {
        return new ApiResponse<>(false, null, errorResponse);
    }
}