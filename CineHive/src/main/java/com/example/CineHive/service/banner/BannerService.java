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

/**
 * 메인 화면 배너와 관련된 비즈니스 로직을 처리하는 서비스입니다.
 * 사용자용 조회 기능과 관리자용 CRUD 기능을 모두 포함합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 모든 메서드는 읽기 전용 트랜잭션으로 동작
public class BannerService {

    private final BannerRepository bannerRepository;

    // =================================================================
    // == 사용자용 로직 (User-facing Logic)
    // =================================================================

    /**
     * 활성화된 모든 배너를 조회합니다.
     * 이 메서드는 메인 화면에 표시될 배너 목록을 가져오는 데 사용됩니다.
     * 배너는 'displayOrder' 필드를 기준으로 오름차순 정렬됩니다.
     *
     * @return 활성화된 배너 정보 DTO 리스트
     */
    public List<BannerResponseDto> findActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(BannerResponseDto::fromEntity) // 엔티티를 응답용 DTO로 변환
                .collect(Collectors.toList());
    }

    // =================================================================
    // == 관리자용 로직 (Admin-facing Logic)
    // =================================================================

    /**
     * [관리자용] 모든 배너(활성/비활성 포함)를 조회합니다.
     * 관리자 페이지에서 전체 배너 목록을 관리할 때 사용됩니다.
     *
     * @return 모든 배너 엔티티 리스트
     */
    public List<Banner> findAllBannersForAdmin() {
        return bannerRepository.findAll();
    }

    /**
     * [관리자용] 새로운 배너를 생성합니다.
     *
     * @param requestDto 생성할 배너의 정보를 담은 요청 DTO
     * @return 데이터베이스에 저장된 새로운 배너 엔티티
     */
    @Transactional // 쓰기 작업이므로 readOnly=false 적용
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

    /**
     * [관리자용] 기존 배너의 정보를 수정합니다.
     *
     * @param bannerId   수정할 배너의 고유 ID
     * @param requestDto 수정할 배너의 새로운 정보를 담은 요청 DTO
     * @return 수정된 배너 엔티티 (JPA의 변경 감지에 의해 DB에 반영됨)
     * @throws IllegalArgumentException 해당 ID의 배너를 찾을 수 없을 경우 발생
     */
    @Transactional // 쓰기 작업이므로 readOnly=false 적용
    public Banner updateBanner(Long bannerId, BannerAdminRequestDto requestDto) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다. ID: " + bannerId));

        // 엔티티의 비즈니스 메서드를 호출하여 상태 변경을 위임
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

    /**
     * [관리자용] 특정 배너를 삭제합니다.
     *
     * @param bannerId 삭제할 배너의 고유 ID
     */
    @Transactional // 쓰기 작업이므로 readOnly=false 적용
    public void deleteBanner(Long bannerId) {
        // ID 존재 여부를 먼저 확인하는 것이 더 안전하지만, deleteById는 내부적으로 조회를 수행합니다.
        bannerRepository.deleteById(bannerId);
    }
}