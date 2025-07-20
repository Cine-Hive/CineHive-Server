package com.example.CineHive.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 모든 API 응답을 위한 표준 래퍼 클래스입니다.
 * 성공/실패 여부와 함께 데이터 또는 에러 정보를 포함합니다.
 *
 * @param <T> 응답 데이터의 타입
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
     * @param data 클라이언트에게 전달할 데이터
     * @return 데이터가 포함된 성공 응답 객체
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 실패 API 응답을 생성합니다.
     * @param code HTTP 상태 코드 또는 커스텀 에러 코드
     * @param message 클라이언트에게 전달할 에러 메시지
     * @return 에러 정보가 포함된 실패 응답 객체
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message));
    }

    /**
     * 에러 정보를 구조적으로 표현하는 내부 레코드입니다.
     */
    private record ErrorResponse(int code, String message) {}
}