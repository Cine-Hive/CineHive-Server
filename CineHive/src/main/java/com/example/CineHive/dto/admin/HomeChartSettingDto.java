package com.example.CineHive.dto.admin;

import com.example.CineHive.dto.media.ChartType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HomeChartSettingDto {

    @NotNull(message = "차트 타입(chartType)은 필수입니다.")
    private ChartType chartType;

    @Min(value = 1, message = "표시 순서(displayOrder)는 1 이상이어야 합니다.")
    private int displayOrder;
}