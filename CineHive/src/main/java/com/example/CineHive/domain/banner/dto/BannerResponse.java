package com.example.CineHive.domain.banner.controller.dto;

import com.example.CineHive.domain.banner.controller.Banner;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 사용자에게 보여줄 배너 정보를 담는 응답 DTO입니다.
 */
@Schema(description = "사용자용 배너 정보 응답")
@Builder
public record BannerResponse(
        @Schema(description = "배너 고유 ID")
        Long id,
        @Schema(description = "배너 제목")
        String title,
        @Schema(description = "배너 부제목")
        String subtitle,
        @Schema(description = "이미지 URL")
        String imageUrl,
        @Schema(description = "링크 URL")
        String linkUrl
) {
    /**
     * Banner 엔티티를 BannerResponse로 변환하는 정적 팩토리 메서드입니다.
     * @param banner 변환할 Banner 엔티티
     * @return 변환된 BannerResponse
     */
    public static BannerResponse from(Banner banner) {
        if (banner == null) {
            return null;
        }

        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .build();
    }
}