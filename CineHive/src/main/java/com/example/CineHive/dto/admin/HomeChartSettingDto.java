package com.example.CineHive.dto.admin;

import com.example.CineHive.dto.media.ChartType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HomeChartSettingDto {
    private ChartType chartType;
    private int displayOrder;
}