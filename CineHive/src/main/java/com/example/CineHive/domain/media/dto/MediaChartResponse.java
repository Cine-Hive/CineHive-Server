package com.example.CineHive.domain.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "차트용 미디어 정보 응답")
@Builder
public record MediaChartResponse(
        @Schema(description = "미디어 고유 ID")
        Long mediaId,
        @Schema(description = "제목")
        String title,
        @Schema(description = "포스터 이미지 경로")
        String posterPath,
        @Schema(description = "평점")
        Double voteAverage,
        @Schema(description = "애니메이션 여부")
        boolean isAnimation,
        @Schema(description = "차트 순위")
        int rank
) {
        /**
         * MediaSummaryResponse와 순위를 조합하여 MediaChartResponse를 생성합니다.
         * @param summary 요약된 미디어 정보
         * @param rank 차트 순위
         * @return 생성된 MediaChartResponse
         */
        public static MediaChartResponse from(MediaSummaryResponse summary, int rank) {
                return MediaChartResponse.builder()
                        .mediaId(summary.id())
                        .title(summary.title())
                        .posterPath(summary.posterPath())
                        .voteAverage(summary.voteAverage())
                        .isAnimation(summary.isAnimation())
                        .rank(rank)
                        .build();
        }
}
