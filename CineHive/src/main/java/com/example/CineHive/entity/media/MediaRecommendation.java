package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_recommendations", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"media_id", "recommended_media_id", "media_type", "recommended_media_type"}
       ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "media_id")
    private Long mediaId;
    
    @Column(name = "recommended_media_id")
    private Long recommendedMediaId;
    
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private Media.MediaType mediaType;
    
    @Column(name = "recommended_media_type")
    @Enumerated(EnumType.STRING)
    private Media.MediaType recommendedMediaType;
    
    @Column(name = "similarity_score")
    private Float similarityScore = 0.0f;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(30); // 기본 30일 유효기간
    
    @Column(name = "access_count")
    private Integer accessCount = 0;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    /**
     * 접근 카운트 증가
     */
    public void incrementAccessCount() {
        this.accessCount = this.accessCount == null ? 1 : this.accessCount + 1;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * 만료일 연장
     */
    public void extendExpiry(int days) {
        this.expiresAt = LocalDateTime.now().plusDays(days);
    }
} 