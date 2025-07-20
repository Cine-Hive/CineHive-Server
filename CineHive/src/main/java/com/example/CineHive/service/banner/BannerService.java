package com.example.CineHive.service.banner;

import com.example.CineHive.dto.banner.BannerAdminRequest;
import com.example.CineHive.dto.banner.BannerResponse;
import com.example.CineHive.entity.banner.Banner;

import java.util.List;

/**
 * 메인 화면 배너와 관련된 비즈니스 로직의 명세를 정의하는 인터페이스입니다.
 */
public interface BannerService {

    // 사용자용 로직
    /**
     * 활성화된 모든 배너를 조회합니다.
     * @return 활성화된 배너 정보 DTO 리스트
     */
    List<BannerResponse> findActiveBanners();

    // 관리자용 로직
    /**
     * [관리자용] 모든 배너(활성/비활성 포함)를 조회합니다.
     * @return 모든 배너 엔티티 리스트
     */
    List<Banner> findAllBannersForAdmin();

    /**
     * [관리자용] 새로운 배너를 생성합니다.
     * @param requestDto 생성할 배너의 정보를 담은 요청 DTO
     * @return 생성된 배너 엔티티
     */
    Banner createBanner(BannerAdminRequest requestDto);

    /**
     * [관리자용] 기존 배너의 정보를 수정합니다.
     * @param bannerId 수정할 배너의 ID
     * @param requestDto 수정할 배너의 새로운 정보
     * @return 수정된 배너 엔티티
     */
    Banner updateBanner(Long bannerId, BannerAdminRequest requestDto);

    /**
     * [관리자용] 특정 배너를 삭제합니다.
     * @param bannerId 삭제할 배너의 ID
     */
    void deleteBanner(Long bannerId);
}
