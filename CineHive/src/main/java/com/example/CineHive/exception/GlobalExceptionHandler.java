package com.example.CineHive.exception;

import com.example.CineHive.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리를 담당하는 클래스.
 * @RestControllerAdvice 어노테이션을 통해 모든 @RestController에서 발생하는 예외를 가로챕니다.
 * 모든 에러 응답은 프로젝트 표준인 ApiResponse 형식으로 통일합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 어노테이션을 통한 유효성 검증 실패 시 발생하는 예외를 처리합니다. (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> String.format("'%s' 필드: %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        log.warn("유효성 검증 실패: {}", errorMessage);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), errorMessage));
    }

    /**
     * @RequestParam 또는 @PathVariable 유효성 검증 실패 시 발생하는 예외를 처리합니다. (400 Bad Request)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("파라미터 유효성 검증 실패: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    /**
     * 조회하려는 리소스를 찾을 수 없을 때 발생하는 예외들을 처리합니다. (404 Not Found)
     * (예: 없는 게시글, 회원, 북마크 조회 시)
     */
    @ExceptionHandler({BoardNotFoundException.class, MemberNotFoundException.class, BookmarkNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(RuntimeException e) {
        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    /**
     * 리소스에 대한 접근 권한이 없을 때 발생하는 예외를 처리합니다. (403 Forbidden)
     */
    @ExceptionHandler(BoardAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(BoardAccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }

    /**
     * 데이터 중복/충돌 시 발생하는 예외들을 처리합니다. (409 Conflict)
     * (예: 이미 북마크한 게시글에 다시 북마크 시도 시)
     */
    @ExceptionHandler({BookmarkAlreadyExistsException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleDataConflictException(RuntimeException e) {
        log.warn("데이터 충돌 발생: {}", e.getMessage());

        String clientMessage;
        if (e instanceof BookmarkAlreadyExistsException) {
            clientMessage = e.getMessage();
        } else {
            clientMessage = "데이터 무결성 제약 조건에 위배되었습니다. (예: 중복된 이메일 또는 닉네임)";
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), clientMessage));
    }

    /**
     * 명시적으로 처리되지 않은 비즈니스 로직 상의 예외(잘못된 인자 등)를 처리합니다. (400 Bad Request)
     */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(RuntimeException e) {
        log.warn("비즈니스 로직 예외: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    /**
     * 유효하지 않은 OAuth 토큰/코드로 인한 예외를 처리합니다. (400 Bad Request)
     */
    @ExceptionHandler(InvalidOAuthTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOAuthToken(InvalidOAuthTokenException e) {
        log.warn("잘못된 OAuth 토큰/코드: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    /**
     * 외부 소셜 플랫폼과의 통신 오류 발생 시 예외를 처리합니다. (503 Service Unavailable)
     */
    @ExceptionHandler(OAuthCommunicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleOAuthCommunication(OAuthCommunicationException e) {
        log.error("OAuth 통신 오류: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage()));
    }

    /**
     * 위에서 처리되지 않은 모든 예외를 처리하는 최종 핸들러입니다. (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부에서 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}