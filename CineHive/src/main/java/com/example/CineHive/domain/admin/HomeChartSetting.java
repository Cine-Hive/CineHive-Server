<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/admin/entity/HomeChartSetting.java
package com.example.CineHive.domain.admin.entity;
=======
package com.example.CineHive.domain.admin;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/admin/HomeChartSetting.java

import com.example.CineHive.domain.media.dto.ChartType;
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