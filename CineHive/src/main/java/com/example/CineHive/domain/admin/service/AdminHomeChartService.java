package com.example.CineHive.domain.admin.service;

import com.example.CineHive.domain.admin.dto.HomeChartSettingRequest;
import com.example.CineHive.domain.admin.dto.HomeChartSettingResponse;
import com.example.CineHive.domain.media.dto.ChartType;

import java.util.List;

/**
 * 관리자용 홈 화면 차트 설정 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface AdminHomeChartService {

    /**
     * 현재 설정된 홈 화면 차트 목록을 DTO 형태로 조회합니다.
     * @return 정렬된 홈 화면 차트 설정 DTO 목록
     */
    List<HomeChartSettingResponse> getHomeChartSettings();

    /**
     * 관리자가 선택 가능한 모든 차트 타입 목록을 제공합니다.
     * @return 사용 가능한 모든 ChartType 목록
     */
    List<ChartType> getAvailableChartTypes();

    /**
     * 홈 화면 차트 설정을 업데이트합니다.
     * @param settings 새로운 차트 설정 DTO 목록
     */
    void updateHomeChartSettings(List<HomeChartSettingRequest> settings);
}