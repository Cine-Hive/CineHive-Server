package com.example.CineHive.domain.admin.controller.dto;

import com.example.CineHive.domain.media.dto.ChartType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 관리자가 홈 화면에 표시될 차트와 순서를 설정하기 위한 요청 DTO입니다.
 */
@Schema(description = "홈 화면 차트 설정 요청 DTO")
public record HomeChartSettingRequest(
        @Schema(description = "홈 화면에 표시할 차트의 종류")
        @NotNull(message = "차트 타입(chartType)은 필수입니다.")
        ChartType chartType,

        @Schema(description = "표시 순서 (낮은 숫자가 먼저 표시됨)", example = "1")
        @NotNull(message = "표시 순서는 필수입니다.")
        @Min(value = 1, message = "표시 순서는 1 이상이어야 합니다.")
        Integer displayOrder
) {}