package com.example.CineHive.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 콘텐츠(게시글, 댓글 등) 신고 시 클라이언트로부터 받는 요청 데이터를 담는 DTO입니다.
 */
@Schema(description = "콘텐츠 신고 요청 DTO")
public record ReportRequest(
        @Schema(description = "신고 사유", example = "부적절한 내용이 포함되어 있습니다.")
        @NotBlank(message = "신고 사유는 필수 입력 항목입니다.")
        @Size(min = 10, max = 500, message = "신고 사유는 10자 이상 500자 이하로 작성해주세요.")
        String reason
) {}