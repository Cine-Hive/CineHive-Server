package com.example.CineHive.service.admin;

import com.example.CineHive.dto.admin.HomeChartSettingRequest;
import com.example.CineHive.dto.admin.HomeChartSettingResponse;
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
public class AdminHomeChartServiceImpl implements AdminHomeChartService {

    private final HomeChartSettingRepository homeChartSettingRepository;

    @Override
    public List<HomeChartSettingResponse> getHomeChartSettings() {
        return homeChartSettingRepository.findAllByOrderByDisplayOrderAsc().stream()
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
        homeChartSettingRepository.deleteAllInBatch();

        List<HomeChartSetting> newSettings = settings.stream()
                .map(dto -> HomeChartSetting.builder()
                        .chartType(dto.chartType())
                        .displayOrder(dto.displayOrder())
                        .build())
                .collect(Collectors.toList());

        homeChartSettingRepository.saveAll(newSettings);
    }
}