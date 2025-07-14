package com.example.CineHive.exception;

import com.example.CineHive.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
     *
     * @param e MethodArgumentNotValidException 객체
     * @return 400 상태 코드와 필드별 상세 에러 메시지
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
     * 조회하려는 리소스(게시글, 회원 등)를 찾을 수 없을 때 발생하는 예외를 처리합니다. (404 Not Found)
     *
     * @param e BoardNotFoundException 또는 MemberNotFoundException 등 RuntimeException을 상속하는 커스텀 예외
     * @return 404 상태 코드와 에러 메시지
     */
    @ExceptionHandler({BoardNotFoundException.class, MemberNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(RuntimeException e) {
        log.warn("리소스를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    /**
     * 리소스에 대한 접근 권한이 없을 때 발생하는 예외를 처리합니다. (403 Forbidden)
     *
     * @param e BoardAccessDeniedException
     * @return 403 상태 코드와 에러 메시지
     */
    @ExceptionHandler(BoardAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(BoardAccessDeniedException e) {
        log.warn("접근 권한 없음: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }

    /**
     * 데이터베이스의 UNIQUE 제약 조건 위반 등 데이터 무결성 관련 예외를 처리합니다. (409 Conflict)
     *
     * @param e DataIntegrityViolationException
     * @return 409 상태 코드와 에러 메시지
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 위반: {}", e.getMessage());
        // 사용자에게는 구체적인 DB 에러 대신 이해하기 쉬운 메시지를 보여줍니다.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "데이터 무결성 제약 조건에 위배되었습니다. (예: 중복된 이메일 또는 닉네임)"));
    }

    /**
     * 명시적으로 처리되지 않은 비즈니스 로직 상의 예외를 처리합니다. (400 Bad Request)
     *
     * @param e IllegalStateException 또는 IllegalArgumentException
     * @return 400 상태 코드와 에러 메시지
     */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(RuntimeException e) {
        log.warn("비즈니스 로직 예외: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    /**
     * 위에서 처리되지 않은 모든 예외를 처리하는 최종 핸들러입니다. (500 Internal Server Error)
     *
     * @param e Exception
     * @return 500 상태 코드와 일반적인 서버 오류 메시지
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception e) {
        log.error("예상치 못한 오류 발생: {}", e.getMessage(), e); // 서버 디버깅을 위해 스택 트레이스를 함께 로깅합니다.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부에서 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}