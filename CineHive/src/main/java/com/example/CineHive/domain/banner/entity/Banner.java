package com.example.CineHive.domain.banner.controller.entity;

import com.example.CineHive.domain.common.controller.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메인 화면 등에 노출될 배너 정보를 관리하는 엔티티입니다.
 */
@Entity
@Table(name = "banners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String linkUrl;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean isActive;

    @Builder
    public Banner(String title, String subtitle, String imageUrl, String linkUrl, int displayOrder, boolean isActive) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    public void update(String title, String subtitle, String imageUrl, String linkUrl, int displayOrder, boolean isActive) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }
}