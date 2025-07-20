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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error