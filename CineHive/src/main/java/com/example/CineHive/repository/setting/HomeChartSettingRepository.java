package com.example.CineHive.repository.setting;

import com.example.CineHive.entity.setting.HomeChartSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeChartSettingRepository extends JpaRepository<HomeChartSetting, Long> {
    // 표시 순서(displayOrder)에 따라 정렬하여 조회
    List<HomeChartSetting> findAllByOrderByDisplayOrderAsc();
}