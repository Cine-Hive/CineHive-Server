package com.example.CineHive.service.admin;

import com.example.CineHive.dto.admin.HomeChartSettingDto;
import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.entity.setting.HomeChartSetting;

import java.util.List;

/**
 * 관리자용 홈 화면 차트 설정 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface AdminHomeChartService {

    /**
     * 현재 설정된 홈 화면 차트 목록을 조회합니다.
     * @return 정렬된 홈 화면 차트 설정 목록
     */
    List<HomeChartSetting> getHomeChartSettings();

    /**
     * 관리자가 선택 가능한 모든 차트 타입 목록을 제공합니다.
     * @return 사용 가능한 모든 ChartType 목록
     */
    List<ChartType> getAvailableChartTypes();

    /**
     * 홈 화면 차트 설정을 업데이트합니다.
     * 이 메서드가 호출되면 'chartSummary' 캐시가 삭제됩니다.
     * @param settings 새로운 차트 설정 DTO 목록
     */
    void updateHomeChartSettings(List<HomeChartSettingDto> settings);
}