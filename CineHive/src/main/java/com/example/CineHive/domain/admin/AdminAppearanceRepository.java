package com.example.CineHive.domain.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 홈 화면 차트 설정(HomeChartSetting) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface AdminAppearanceRepository extends JpaRepository<HomeChartSetting, Long> {

    /**
     * 모든 홈 화면 차트 설정을 'displayOrder' 필드를 기준으로 오름차순 정렬하여 조회합니다.
     *
     * @return 정렬된 홈 화면 차트 설정 엔티티의 리스트
     */
    List<HomeChartSetting> findAllByOrderByDisplayOrderAsc();
}
