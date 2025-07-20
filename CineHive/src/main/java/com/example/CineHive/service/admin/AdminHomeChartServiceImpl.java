package com.example.CineHive.service.admin;

import com.example.CineHive.dto.admin.HomeChartSettingRequest;
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

@Service // @Service 어노테이션은 구현체에 붙입니다.
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminHomeChartServiceImpl implements AdminHomeChartService { // 인터페이스를 구현합니다.

    private final HomeChartSettingRepository homeChartSettingRepository;

    @Override // @Override 어노테이션 추가
    public List<HomeChartSetting> getHomeChartSettings() {
        return homeChartSettingRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override // @Override 어노테이션 추가
    public List<ChartType> getAvailableChartTypes() {
        return Arrays.asList(ChartType.values());
    }

    @Override // @Override 어노테이션 추가
    @Transactional // 클래스 레벨의 readOnly=true를 덮어쓰기 위해 필요합니다.
    @CacheEvict(value = "chartSummary", allEntries = true)
    public void updateHomeChartSettings(List<HomeChartSettingRequest> settings) {
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