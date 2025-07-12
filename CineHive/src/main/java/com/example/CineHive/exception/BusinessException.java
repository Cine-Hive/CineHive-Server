package com.example.CineHive.exception;

import lombok.Getter;

/**
 * 모든 비즈니스 관련 커스텀 예외의 부모 클래스입니다.
 * 모든 비즈니스 예외는 ErrorCode를 포함해야 합니다.
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode를 기반으로 예외를 생성합니다.
     * @param errorCode 에러 코드 열거형
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 함께 동적인 상세 메시지를 포함하여 예외를 생성합니다.
     * @param message 상세 에러 메시지 (주로 로깅용)
     * @param errorCode 에러 코드 열거형
     */
    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}