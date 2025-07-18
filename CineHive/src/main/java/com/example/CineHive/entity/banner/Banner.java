package com.example.CineHive.entity.banner;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "banners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 배너에 표시될 주 텍스트

    private String subtitle; // 배너에 표시될 부가 텍스트 (선택)

    @Column(nullable = false)
    private String imageUrl; // 배너 이미지의 부분 URL

    @Column(nullable = false)
    private String linkUrl; // 배너 클릭 시 이동할 URL (예: /media/movie/550)

    @Column(nullable = false)
    private int displayOrder; // 표시 순서 (낮은 숫자가 먼저)

    @Column(nullable = false)
    private boolean isActive = true; // 노출 여부 (기본값: 노출)

    @Builder
    public Banner(String title, String subtitle, String imageUrl, String linkUrl, int displayOrder, boolean isActive) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    // 정보 수정을 위한 메서드
    public void update(String title, String subtitle, String imageUrl, String linkUrl, int displayOrder, boolean isActive) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }
}