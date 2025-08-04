package com.example.CineHive.domain.report.dto;

<<<<<<< HEAD
import com.example.CineHive.domain.report.entity.Report;
import com.example.CineHive.domain.report.entity.ReportStatus;
=======
import com.example.CineHive.domain.report.Report;
import com.example.CineHive.domain.report.ReportStatus;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리)
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.Instant;

@Schema(description = "관리자용 신고 내역 조회 응답 DTO")
@Builder
public record ReportResponse(
        @Schema(description = "신고 고유 ID")
        Long reportId,
        @Schema(description = "신고된 콘텐츠의 ID (게시글 또는 댓글 ID)")
        Long reportedContentId,
        @Schema(description = "신고된 콘텐츠의 타입", example = "POST")
        String contentType,
        @Schema(description = "신고 사유")
        String reason,
        @Schema(description = "신고자 닉네임")
        String reporterNickname,
        @Schema(description = "신고 처리 상태")
        ReportStatus status,
        @Schema(description = "신고 접수 시각")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt
) {
    public static ReportResponse from(Report report) {
        Long contentId;
        String type;
        if (report.getPost() != null) {
            contentId = report.getPost().getId();
            type = "POST";
        } else if (report.getComment() != null) {
            contentId = report.getComment().getId();
            type = "COMMENT";
        } else {
            contentId = null;
            type = "UNKNOWN";
        }
        return ReportResponse.builder()
                .reportId(report.getId())
                .reportedContentId(contentId)
                .contentType(type)
                .reason(report.getReason())
                .reporterNickname(report.getReporter().getNickname())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}