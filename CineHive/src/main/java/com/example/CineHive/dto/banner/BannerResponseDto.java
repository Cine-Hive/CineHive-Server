package com.example.CineHive.dto.banner;

import com.example.CineHive.entity.banner.Banner;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 사용자에게 보여줄 배너 정보를 담는 응답 DTO입니다.
 * 활성화된 배너 목록 조회 시 사용됩니다.
 */
@Schema(description = "사용자용 배너 정보 응답 DTO")
@Builder
public record BannerResponseDto(
        @Schema(description = "배너 고유 ID")
        Long id,

        @Schema(description = "배너의 메인 제목")
        String title,

        @Schema(description = "배너의 부제목")
        String subtitle,

        @Schema(description = "배너 이미지 URL")
        String imageUrl,

        @Schema(description = "배너 클릭 시 이동할 링크 URL")
        String linkUrl
) {
    /**
     * Banner 엔티티를 BannerResponseDto로 변환하는 정적 팩토리 메서드입니다.
     * 이 메서드는 서비스 계층이나 매퍼에서 사용될 수 있습니다.
     *
     * @param banner 변환할 Banner 엔티티
     * @return 변환된 BannerResponseDto
     */
    public static BannerResponseDto fromEntity(Banner banner) {
        if (banner == null) {
            return null;
        }

        return BannerResponseDto.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .build();
    }
}