package com.example.CineHive.domain.admin.dto;

import com.example.CineHive.domain.media.dto.ChartType;
import com.example.CineHive.domain.admin.HomeChartSetting;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 홈 화면 차트 설정 조회 응답 DTO입니다.
 */
@Schema(description = "홈 화면 차트 설정 조회 응답")
public record HomeChartSettingResponse(
        @Schema(description = "차트 종류")
        ChartType chartType,
        @Schema(description = "차트 제목")
        String chartTitle,
        @Schema(description = "표시 순서")
        int displayOrder
) {
    public static HomeChartSettingResponse from(HomeChartSetting entity) {
        return new HomeChartSettingResponse(
                entity.getChartType(),
                entity.getChartType().getDescription(),
                entity.getDisplayOrder()
        );
    }
}