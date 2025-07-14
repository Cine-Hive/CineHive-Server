package com.example.CineHive.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 모든 API 응답을 감싸는 제네릭 래퍼 클래스. (개선된 구조)
 * 성공 시에는 data 필드에 결과가 담기고, 실패 시에는 error 필드에 에러 정보가 담깁니다.
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
public class ApiResponse<T> {

    private final boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL) // data가 null이면 JSON 응답에서 제외
    private final T data;

    @JsonInclude(JsonInclude.Include.NON_NULL) // error가 null이면 JSON 응답에서 제외
    private final ErrorResponse error;

    // 생성자 직접 호출을 막고, 정적 팩토리 메서드 사용을 유도
    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /**
     * 성공 응답을 생성합니다. (기존 ok 메서드와 유사)
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * 실패 응답을 생성합니다. (새로운 error 메서드)
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message));
    }

    /**
     * 에러 정보를 구조적으로 담는 내부 클래스.
     */
    @Getter
    private static class ErrorResponse {
        private final int code;
        private final String message;

        private ErrorResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}