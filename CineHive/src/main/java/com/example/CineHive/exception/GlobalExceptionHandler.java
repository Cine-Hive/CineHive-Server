package com.example.CineHive.exception;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 직접 정의한 모든 비즈니스 예외를 처리합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * @Valid DTO 유효성 검증 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("유효성 검증 실패: {}", errorMessage);
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Validation failed", errorMessage, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorResponse));
    }

    /**
     * @RequestParam, @PathVariable 등에서의 유효성 검증 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("파라미터 유효성 검증 실패: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Invalid parameter", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorResponse));
    }

    /**
     * 잘못된 HTTP 요청 파라미터 관련 예외를 처리합니다.
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestParameter(Exception e, HttpServletRequest request) {
        log.warn("잘못된 요청 파라미터: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Invalid parameter", "요청 파라미터가 유효하지 않습니다.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorResponse));
    }

    /**
     * 잘못된 JSON 형식의 요청 본문으로 인한 파싱 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleParseError(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("JSON 파싱 오류: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Invalid JSON", "유효하지 않은 요청 형식입니다.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorResponse));
    }

    /**
     * 데이터베이스 무결성 제약 조건 위반 시 예외를 처리합니다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("데이터 무결성 위반: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.CONFLICT.value(), "Data integrity violation", "데이터 무결성 제약 조건에 위배되었습니다.", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(errorResponse));
    }

    /**
     * 위에서 처리되지 않은 모든 서버 내부 예외를 처리하는 최종 핸들러입니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 발생", e);
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(errorResponse));
    }
}