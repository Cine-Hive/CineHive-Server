<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/admin/service/AdminHomeChartServiceImpl.java
package com.example.CineHive.domain.admin.service;

import com.example.CineHive.domain.admin.entity.HomeChartSetting;
import com.example.CineHive.domain.admin.repository.AdminAppearanceRepository;
=======
package com.example.CineHive.domain.admin;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/admin/AdminHomeChartServiceImpl.java

import com.example.CineHive.domain.admin.dto.HomeChartSettingRequest;
import com.example.CineHive.domain.admin.dto.HomeChartSettingResponse;
import com.example.CineHive.domain.media.dto.ChartType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminHomeChartServiceImpl implements AdminHomeChartService {

    private final AdminAppearanceRepository adminAppearanceRepository;

    @Override
    public List<HomeChartSettingResponse> getHomeChartSettings() {
        return adminAppearanceRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(HomeChartSettingResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChartType> getAvailableChartTypes() {
        return Arrays.asList(ChartType.values());
    }

    @Override
    @Transactional
    @CacheEvict(value = "chartSummary", allEntries = true)
    public void updateHomeChartSettings(List<HomeChartSettingRequest> settings) {
        adminAppearanceRepository.deleteAllInBatch();

        List<HomeChartSetting> newSettings = settings.stream()
                .map(dto -> HomeChartSetting.builder()
                        .chartType(dto.chartType())
                        .displayOrder(dto.displayOrder())
                        .build())
                .collect(Collectors.toList());

        adminAppearanceRepository.saveAll(newSettings);
        log.info("관리자에 의해 홈 화면 차트 설정이 업데이트되었습니다. 총 {}개의 차트가 설정되었습니다.", newSettings.size());
    }
}