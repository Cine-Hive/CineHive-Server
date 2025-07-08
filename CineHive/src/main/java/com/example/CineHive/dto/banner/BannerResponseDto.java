package com.example.CineHive.dto.banner;

import com.example.CineHive.entity.banner.Banner;
import lombok.Getter;

@Getter
public class BannerResponseDto {
    private final Long id;
    private final String title;
    private final String subtitle;
    private final String imageUrl;
    private final String linkUrl;

    public BannerResponseDto(Banner banner) {
        this.id = banner.getId();
        this.title = banner.getTitle();
        this.subtitle = banner.getSubtitle();
        this.imageUrl = banner.getImageUrl();
        this.linkUrl = banner.getLinkUrl();
    }
}