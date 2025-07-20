package com.example.CineHive.dto.response;

import com.example.CineHive.dto.media.ChartSection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "홈 화면 전체 차트 요약 정보 응답")
public record ChartSummaryResponse(
        List<ChartSection> sections
) {}