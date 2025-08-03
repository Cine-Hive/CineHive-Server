package com.example.CineHive.domain.banner.repository;

import com.example.CineHive.domain.banner.entity.Banner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 배너(Banner) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 * Spring Data JPA가 기본적인 CRUD 메서드를 자동으로 생성해줍니다.
 */
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * 활성화된(isActive = true) 모든 배너를 조회합니다.
     * 결과는 'displayOrder' 필드를 기준으로 오름차순 정렬됩니다.
     * 이 메서드는 메인 화면에 표시될 배너 목록을 가져오는 데 사용됩니다.
     *
     * @return 정렬된 활성 배너 엔티티의 리스트
     */
    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();
}
