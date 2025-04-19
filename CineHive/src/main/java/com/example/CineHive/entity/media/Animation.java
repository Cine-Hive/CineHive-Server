package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "animations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Animation extends Media {
    
    // 애니메이션 특화 필드
    private String originalTitle;
    private String originalLanguage;
    
    // 영화 타입 애니메이션인지 TV 타입 애니메이션인지 구분
    @Enumerated(EnumType.STRING)
    private AnimationType animationType;
    
    // 만약 TV 시리즈라면 시즌, 에피소드 정보
    private Integer numberOfSeasons;
    private Integer numberOfEpisodes;
    
    public enum AnimationType {
        MOVIE, TV
    }
    
    // 기본 생성자
    public Animation(Long id) {
        super();
        setId(id);
        setMediaType(MediaType.ANIMATION);
        setCategory(MediaCategory.DEFAULT);
    }
} 