package com.example.CineHive.service.banner;

import com.example.CineHive.dto.banner.BannerAdminRequest;
import com.example.CineHive.dto.banner.BannerAdminResponse;
import com.example.CineHive.dto.banner.BannerResponse;
import com.example.CineHive.entity.banner.Banner;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.banner.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        return BannerAdminResponse.from(savedBanner);
    }

    @Override
    @Transactional
    public BannerAdminResponse updateBanner(Long bannerId, BannerAdminRequest request) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BANNER_NOT_FOUND));

        banner.update(
                request.title(),
                request.subtitle(),
                request.imageUrl(),
                request.linkUrl(),
                request.displayOrder(),
                request.isActive()
        );
        return BannerAdminResponse.from(banner); // 수정 후 DTO로 변환하여 반환
    }

    @Override
    @Transactional
    public void deleteBanner(Long bannerId) {
        if (!bannerRepository.existsById(bannerId)) {
            throw new BusinessException(ErrorCode.BANNER_NOT_FOUND);
        }
        bannerRepository.deleteById(bannerId);
    }
}