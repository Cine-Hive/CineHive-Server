package com.example.CineHive.exception;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.ErrorResponse;
import com.example.CineHive.dto.global.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 직접 정의한 BusinessException을 처리합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.name(),
                errorCode.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * @Valid DTO 유효성 검증 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldErrorDetail> details = bindingResult.getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : "",
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String errorMessage = details.stream().map(FieldErrorDetail::reason).collect(Collectors.joining(", "));
        log.warn("유효성 검증 실패: {}", errorMessage);

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                "Validation Failed",
                errorMessage, // 여러 필드 에러 메시지를 하나로 합쳐서 전달
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * @RequestParam, @PathVariable 등에서의 유효성 검증 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("파라미터 유효성 검증 실패: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus().value(), errorCode.getCode(), "Invalid Parameter", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * 필수 요청 파라미터 누락 또는 타입 불일치 예외를 처리합니다.
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestParameter(Exception e, HttpServletRequest request) {
        log.warn("잘못된 요청 파라미터: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus().value(), errorCode.getCode(), "Invalid Parameter", "요청 파라미터가 유효하지 않습니다.", request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * 잘못된 JSON 형식의 요청 본문으로 인한 파싱 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleParseError(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("JSON 파싱 오류: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus().value(), errorCode.getCode(), "Invalid JSON Format", "유효하지 않은 요청 형식입니다.", request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * 데이터베이스 무결성 제약 조건 위반 시 예외를 처리합니다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("데이터 무결성 위반: {}", e.getMessage());
        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus().value(), errorCode.getCode(), errorCode.name(), errorCode.getMessage(), request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * 위에서 처리되지 않은 모든 서버 내부 예외를 처리하는 최종 핸들러입니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 발생", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getStatus().value(), errorCode.getCode(), "Internal Server Error", errorCode.getMessage(), request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }
}