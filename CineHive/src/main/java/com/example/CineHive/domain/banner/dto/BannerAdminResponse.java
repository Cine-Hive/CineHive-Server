package com.example.CineHive.domain.banner.dto;

import com.example.CineHive.domain.banner.Banner;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 관리자에게 보여줄 배너 상세 정보를 담는 응답 DTO입니다.
 */
@Schema(description = "관리자용 배너 정보 응답")
public record BannerAdminResponse(
        @Schema(description = "배너 고유 ID")
        Long id,
        @Schema(description = "배너 제목")
        String title,
        @Schema(description = "배너 부제목")
        String subtitle,
        @Schema(description = "이미지 URL")
        String imageUrl,
        @Schema(description = "링크 URL")
        String linkUrl,
        @Schema(description = "표시 순서")
        int displayOrder,
        @Schema(description = "활성화 여부")
        boolean isActive
) {
    /**
     * Banner 엔티티를 BannerAdminResponse DTO로 변환합니다.
     */
    public static BannerAdminResponse from(Banner banner) {
        return new BannerAdminResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getImageUrl(),
                banner.getLinkUrl(),
                banner.getDisplayOrder(),
                banner.isActive()
        );
    }
}