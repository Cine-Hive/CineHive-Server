package com.example.CineHive.service.banner;

import com.example.CineHive.dto.banner.BannerAdminRequestDto;
import com.example.CineHive.dto.banner.BannerResponseDto;
import com.example.CineHive.entity.banner.Banner;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.banner.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BannerService 인터페이스의 구현체입니다.
 * 메인 화면 배너와 관련된 실제 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    // =================================================================
    // == 사용자용 로직 (User-facing Logic)
    // =================================================================

    @Override
    public List<BannerResponseDto> findActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(BannerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // =================================================================
    // == 관리자용 로직 (Admin-facing Logic)
    // =================================================================

    @Override
    public List<Banner> findAllBannersForAdmin() {
        return bannerRepository.findAll();
    }

    @Override
    @Transactional
    public Banner createBanner(BannerAdminRequestDto requestDto) {
        Banner banner = Banner.builder()
                .title(requestDto.title())
                .subtitle(requestDto.subtitle())
                .imageUrl(requestDto.imageUrl())
                .linkUrl(requestDto.linkUrl())
                .displayOrder(requestDto.displayOrder())
                .isActive(requestDto.isActive())
                .build();
        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public Banner updateBanner(Long bannerId, BannerAdminRequestDto requestDto) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BANNER_NOT_FOUND));

        banner.update(
                requestDto.title(),
                requestDto.subtitle(),
                requestDto.imageUrl(),
                requestDto.linkUrl(),
                requestDto.displayOrder(),
                requestDto.isActive()
        );
        return banner;
    }

    @Override
    @Transactional
    public void deleteBanner(Long bannerId) {
        // 삭제하려는 배너가 존재하는지 먼저 확인
        if (!bannerRepository.existsById(bannerId)) {
            throw new BusinessException(ErrorCode.BANNER_NOT_FOUND);
        }
        bannerRepository.deleteById(bannerId);
    }
}
