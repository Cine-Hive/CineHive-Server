<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/banner/service/BannerServiceImpl.java
package com.example.CineHive.domain.banner.service;

import com.example.CineHive.domain.banner.entity.Banner;
import com.example.CineHive.domain.banner.repository.BannerRepository;
=======
package com.example.CineHive.domain.banner;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/banner/BannerServiceImpl.java

import com.example.CineHive.domain.banner.dto.BannerAdminRequest;
import com.example.CineHive.domain.banner.dto.BannerAdminResponse;
import com.example.CineHive.domain.banner.dto.BannerResponse;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    public List<BannerResponse> findActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(BannerResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<BannerAdminResponse> findAllBannersForAdmin() {
        return bannerRepository.findAll().stream()
                .map(BannerAdminResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BannerAdminResponse createBanner(BannerAdminRequest request) {
        Banner banner = Banner.builder()
                .title(request.title())
                .subtitle(request.subtitle())
                .imageUrl(request.imageUrl())
                .linkUrl(request.linkUrl())
                .displayOrder(request.displayOrder())
                .isActive(request.isActive())
                .build();
        Banner savedBanner = bannerRepository.save(banner);
        log.info("새로운 배너가 생성되었습니다. ID: {}, 제목: {}", savedBanner.getId(), savedBanner.getTitle());
        return BannerAdminResponse.from(savedBanner);
    }

    @Override
    @Transactional
    public BannerAdminResponse updateBanner(Long bannerId, BannerAdminRequest request) {
        Banner banner = findBannerById(bannerId);

        banner.update(
                request.title(),
                request.subtitle(),
                request.imageUrl(),
                request.linkUrl(),
                request.displayOrder(),
                request.isActive()
        );
        log.info("배너 정보가 수정되었습니다. ID: {}", bannerId);
        return BannerAdminResponse.from(banner);
    }

    @Override
    @Transactional
    public void deleteBanner(Long bannerId) {
        Banner banner = findBannerById(bannerId);
        bannerRepository.delete(banner);
        log.info("배너가 삭제되었습니다. ID: {}", bannerId);
    }

    private Banner findBannerById(Long bannerId) {
        return bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BANNER_NOT_FOUND));
    }
}