package com.example.CineHive.dto.media;

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
) {}