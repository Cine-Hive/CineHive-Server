package com.example.CineHive.exception;

import com.example.CineHive.dto.response.ApiResponse;
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

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 직접 정의한 모든 비즈니스 예외를 처리합니다.
     * @param e BusinessException 또는 그 하위 예외
     * @return ErrorCode에 정의된 상태 코드와 메시지를 포함한 ResponseEntity
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: {}", e.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getStatus().value(), errorCode.getMessage()));
    }

    /**
     * @Valid 어노테이션을 통한 DTO 유효성 검증 실패 시 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("유효성 검증 실패: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), errorMessage));
    }

    /**
     * @RequestParam, @PathVariable 등에서의 유효성 검증 실패 시 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("파라미터 유효성 검증 실패: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    /**
     * 잘못된 HTTP 요청 파라미터 관련 예외를 처리합니다.
     * (필수 파라미터 누락, 파라미터 타입 불일치 등)
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestParameter(Exception e) {
        log.warn("잘못된 요청 파라미터: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "요청 파라미터가 유효하지 않습니다."));
    }

    /**
     * 잘못된 JSON 형식의 요청 본문으로 인한 파싱 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleParseError(HttpMessageNotReadableException e) {
        log.warn("JSON 파싱 오류: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 요청 형식입니다."));
    }

    /**
     * 데이터베이스 무결성 제약 조건 위반 시 예외를 처리합니다. (e.g., Unique Key 중복)
     * 서비스 레이어에서 더 구체적인 BusinessException으로 변환하는 것을 권장합니다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 위반: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "데이터 무결성 제약 조건에 위배되었습니다."));
    }

    /**
     * 위에서 처리되지 않은 모든 서버 내부 예외를 처리하는 최종 핸들러입니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}