package com.example.CineHive.global.exception;

import lombok.Getter;

/**
 * 서비스의 모든 비즈니스 로직 관련 예외를 대표하는 클래스입니다.
 * 모든 커스텀 예외는 이 클래스를 상속받아 구현할 수 있으며,
 * 반드시 ErrorCode를 포함해야 합니다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * ErrorCode에 정의된 기본 메시지를 사용하여 예외를 생성합니다.
     * @param errorCode 발생한 에러의 종류를 정의하는 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode와 함께 동적인 상세 메시지를 포함하여 예외를 생성합니다.
     * 이 메시지는 주로 서버 내부 로깅용으로 사용됩니다.
     * @param message 개발자 확인을 위한 상세 에러 메시지
     * @param errorCode 발생한 에러의 종류를 정의하는 에러 코드
     */
    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    // 원인 예외(cause)를 포함하는 생성자 추가
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}