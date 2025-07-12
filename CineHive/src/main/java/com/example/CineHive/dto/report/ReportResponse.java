package com.example.CineHive.dto.report;

import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.board.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "관리자용 신고 내역 조회 응답 DTO")
@Builder
public record ReportResponse(
        @Schema(description = "신고 고유 ID")
        Long reportId,

        @Schema(description = "신고된 콘텐츠의 ID (게시글 또는 댓글 ID)")
        Long reportedContentId,

        @Schema(description = "신고된 콘텐츠의 타입", example = "BOARD")
        String contentType,

        @Schema(description = "신고 사유")
        String reason,

        @Schema(description = "신고자 닉네임")
        String reporterNickname,

        @Schema(description = "신고 처리 상태")
        ReportStatus status,

        @Schema(description = "신고 접수 시각")
        LocalDateTime createdAt
) {
    /**
     * Report 엔티티를 ReportResponse DTO로 변환하는 정적 팩토리 메서드입니다.
     * @param report 변환할 Report 엔티티
     * @return 변환된 ReportResponse DTO
     */
    public static ReportResponse fromEntity(Report report) {
        Long contentId;
        String type;

        if (report.getBoard() != null) {
            contentId = report.getBoard().getId();
            type = "BOARD";
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