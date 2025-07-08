package com.example.CineHive.service.banner;

import com.example.CineHive.entity.banner.Banner;
import com.example.CineHive.dto.banner.BannerAdminRequestDto;
import com.example.CineHive.dto.banner.BannerResponseDto;
import com.example.CineHive.repository.banner.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    // --- 사용자용 조회 로직 ---
    public List<BannerResponseDto> findActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(BannerResponseDto::new)
                .collect(Collectors.toList());
    }

    // --- 관리자용 CRUD 로직 ---
    public List<Banner> findAllBannersForAdmin() {
        return bannerRepository.findAll();
    }

    @Transactional
    public Banner createBanner(BannerAdminRequestDto requestDto) {
        Banner banner = Banner.builder()
                .title(requestDto.getTitle())
                .subtitle(requestDto.getSubtitle())
                .imageUrl(requestDto.getImageUrl())
                .linkUrl(requestDto.getLinkUrl())
                .displayOrder(requestDto.getDisplayOrder())
                .isActive(requestDto.isActive())
                .build();
        return bannerRepository.save(banner);
    }

    @Transactional
    public Banner updateBanner(Long bannerId, BannerAdminRequestDto requestDto) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found with id: " + bannerId));

        banner.update(
                requestDto.getTitle(),
                requestDto.getSubtitle(),
                requestDto.getImageUrl(),
                requestDto.getLinkUrl(),
                requestDto.getDisplayOrder(),
                requestDto.isActive()
        );
        return banner;
    }

    @Transactional
    public void deleteBanner(Long bannerId) {
        bannerRepository.deleteById(bannerId);
    }
}