package com.example.CineHive.entity.setting;

import com.example.CineHive.dto.media.ChartType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "home_chart_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeChartSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ChartType chartType;

    @Column(nullable = false)
    private int displayOrder; // 표시 순서

    @Builder
    public HomeChartSetting(ChartType chartType, int displayOrder) {
        this.chartType = chartType;
        this.displayOrder = displayOrder;
    }
}