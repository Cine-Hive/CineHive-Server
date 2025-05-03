package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @Id
    private Integer id;
    
    private String name;
    
    // 호환성을 위해 유지하지만 모든 장르는 COMMON으로 통합
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType = MediaType.COMMON;
    
    public enum MediaType {
        MOVIE, TV, ANIMATION, COMMON
    }
} 