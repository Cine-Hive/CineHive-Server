package com.example.CineHive.dto.response;

import com.example.CineHive.dto.media.MediaChartResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "홈 화면을 구성하는 개별 차트 섹션")
@Builder
public record ChartSection(
        @Schema(description = "차트 타입 이름", example = "TRENDING_MOVIES_WEEK")
        String chartType,
        @Schema(description = "차트 제목", example = "주간 트렌드 영화")
        String title,
        @Schema(description = "차트 콘텐츠 목록 (미디어 정보)")
        List<MediaChartResponse> content
) {}