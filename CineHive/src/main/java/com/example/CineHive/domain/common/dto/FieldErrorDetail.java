package com.example.CineHive.domain.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 유효성 검증(Validation) 실패 시, 어떤 필드가 왜 실패했는지 상세 정보를 담는 DTO입니다.
 */
@Schema(description = "필드 에러 상세 정보")
public record FieldErrorDetail(
        @Schema(description = "오류가 발생한 필드 이름")
        String field,
        @Schema(description = "오류의 원인이 된 입력값")
        String rejectedValue,
        @Schema(description = "오류 발생 이유")
        String reason
) {}