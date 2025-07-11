package com.example.CineHive.service.admin;

import com.example.CineHive.dto.admin.HomeChartSettingDto;
import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.entity.setting.HomeChartSetting;
import com.example.CineHive.repository.setting.HomeChartSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSettingService {

    private final HomeChartSettingRepository homeChartSettingRepository;

    /**
     * 현재 설정된 홈 화면 차트 목록을 조회합니다.
     */
    public List<HomeChartSetting> getHomeChartSettings() {
        return homeChartSettingRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 관리자가 선택 가능한 모든 차트 타입 목록을 제공합니다.
     */
    public List<ChartType> getAvailableChartTypes() {
        return Arrays.asList(ChartType.values());
    }

    /**
     * 홈 화면 차트 설정을 업데이트합니다.
     * 이 메서드가 호출되면 'chartSummary' 캐시가 삭제됩니다.
     */
    @Transactional
    @CacheEvict(value = "chartSummary", allEntries = true)
    public void updateHomeChartSettings(List<HomeChartSettingDto> settings) {
        // 기존 설정을 모두 삭제
        homeChartSettingRepository.deleteAllInBatch();

        // 새로운 설정으로 다시 저장
        List<HomeChartSetting> newSettings = settings.stream()
                .map(dto -> HomeChartSetting.builder()
                        .chartType(dto.getChartType())
                        .displayOrder(dto.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        homeChartSettingRepository.saveAll(newSettings);
    }
}