package com.example.CineHive.global.exception;

import com.example.CineHive.global.dto.ApiResponse;
import com.example.CineHive.global.dto.ErrorResponse;
import com.example.CineHive.global.dto.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 직접 정의한 BusinessException을 처리합니다.
     * 예외에 포함된 ErrorCode를 사용하여 응답을 생성합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException Occurred: {}, URI: {}", e.getMessage(), request.getRequestURI());
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.name(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * @Valid 어노테이션으로 인한 DTO 유효성 검증 실패 시 예외를 처리합니다.
     * 어떤 필드가 왜 실패했는지 상세 정보를 동적으로 생성하여 제공하는 것이 더 유용하므로, 이 핸들러는 동적 메시지를 유지합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldErrorDetail> details = bindingResult.getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : null,
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        // 여러 필드 에러 메시지를 하나의 문자열로 결합하여 로그 및 응답에 사용합니다.
        String dynamicErrorMessage = details.stream()
                .map(detail -> String.format("'%s' 필드: %s", detail.field(), detail.reason()))
                .collect(Collectors.joining(", "));
        log.warn("Validation Failed: {}, URI: {}", dynamicErrorMessage, request.getRequestURI());

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.name(),
                dynamicErrorMessage,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * RestTemplate을 사용한 외부 API 통신 중 발생하는 예외를 처리합니다.
     * 주로 OAuth2 클라이언트에서 사용됩니다.
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClientException(RestClientException e, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.OAUTH_COMMUNICATION_ERROR;

        // 4xx, 5xx 에러코드를 포함하는 경우, 더 상세한 로그를 남깁니다.
        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException hce = (HttpClientErrorException) e;
            log.error("External API Client Error: Status={}, Body={}, URI: {}",
                    hce.getStatusCode(), hce.getResponseBodyAsString(), request.getRequestURI(), hce);
        } else {
            log.error("External API I/O Error: {}, URI: {}", e.getMessage(), request.getRequestURI(), e);
        }

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
     * @RequestParam, @PathVariable 등에서의 유효성 검증 실패 시 예외를 처리합니다.
     * ConstraintViolationException은 위반에 대한 상세 정보를 메시지로 제공하므로, 이를 활용합니다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("Parameter Constraint Violation: {}, URI: {}", e.getMessage(), request.getRequestURI());
        ErrorCode errorCode = ErrorCode.CONSTRAINT_VIOLATION;
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.name(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * 지원하지 않는 HTTP 메서드로 요청 시 예외를 처리합니다.
     * ※ 참고: ErrorCode Enum에 아래 코드를 추가해주세요.
     * METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C009", "지원하지 않는 HTTP 메서드입니다."),
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("Method Not Supported: {}, URI: {}", e.getMessage(), request.getRequestURI());
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.name(),
                "지원하지 않는 HTTP 메서드입니다.", // errorCode.getMessage()
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * 필수 요청 파라미터 누락 또는 타입 불일치 예외를 처리합니다.
     * ErrorCode에 정의된 일관된 메시지를 사용합니다.
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestParameter(Exception e, HttpServletRequest request) {
        log.warn("Invalid Request Parameter: {}, URI: {}", e.getMessage(), request.getRequestURI());
        ErrorCode errorCode = ErrorCode.MISSING_REQUEST_PARAMETER;
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
     * 잘못된 JSON 형식의 요청 본문으로 인한 파싱 실패 시 예외를 처리합니다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleParseError(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("JSON Parsing Error: {}, URI: {}", e.getMessage(), request.getRequestURI());
        ErrorCode errorCode = ErrorCode.INVALID_JSON_FORMAT;
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
     * 데이터베이스 무결성 제약 조건 위반 시 예외를 처리합니다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("Data Integrity Violation: {}, URI: {}", e.getMessage(), request.getRequestURI());
        ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.name(),
                errorCode.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }

    /**
     * 위에서 처리되지 않은 모든 서버 내부 예외를 처리하는 최종 핸들러입니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception e, HttpServletRequest request) {
        log.error("An unhandled exception occurred: {}", e.getMessage(), e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.name(),
                errorCode.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorResponse));
    }
}
