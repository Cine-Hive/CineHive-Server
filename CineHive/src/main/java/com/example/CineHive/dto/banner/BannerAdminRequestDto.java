package com.example.CineHive.dto.banner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannerAdminRequestDto {
    private String title;
    private String subtitle;
    private String imageUrl;
    private String linkUrl;
    private int displayOrder;
    private boolean isActive;
}