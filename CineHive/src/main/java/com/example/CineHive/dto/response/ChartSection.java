package com.example.CineHive.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChartSection {
    private String chartType; // e.g., "TRENDING_MOVIES_WEEK"
    private String title;     // e.g., "주간 트렌드 영화"
    private List<MediaChartDto> content;
}