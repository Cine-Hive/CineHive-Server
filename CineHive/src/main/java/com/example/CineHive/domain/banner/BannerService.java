<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/banner/service/BannerService.java
package com.example.CineHive.domain.banner.service;
=======
package com.example.CineHive.domain.banner;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/banner/BannerService.java

import com.example.CineHive.domain.banner.dto.BannerAdminRequest;
import com.example.CineHive.domain.banner.dto.BannerAdminResponse;
import com.example.CineHive.domain.banner.dto.BannerResponse;

import java.util.List;

/**
 * 메인 화면 배너와 관련된 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface BannerService {

    /**
     * 활성화된 모든 배너를 조회합니다.
     * @return 활성화된 배너 정보 DTO 리스트
     */
    List<BannerResponse> findActiveBanners();

    /**
     * [관리자용] 모든 배너를 조회합니다.
     * @return 모든 배너 정보 DTO 리스트
     */
    List<BannerAdminResponse> findAllBannersForAdmin();

    /**
     * [관리자용] 새로운 배너를 생성합니다.
     * @param request 생성할 배너의 정보를 담은 요청 DTO
     * @return 생성된 배너 정보 DTO
     */
    BannerAdminResponse createBanner(BannerAdminRequest request);

    /**
     * [관리자용] 기존 배너의 정보를 수정합니다.
     * @param bannerId 수정할 배너의 ID
     * @param request 수정할 배너의 새로운 정보
     * @return 수정된 배너 정보 DTO
     */
    BannerAdminResponse updateBanner(Long bannerId, BannerAdminRequest request);

    /**
     * [관리자용] 특정 배너를 삭제합니다.
     * @param bannerId 삭제할 배너의 ID
     */
    void deleteBanner(Long bannerId);
}