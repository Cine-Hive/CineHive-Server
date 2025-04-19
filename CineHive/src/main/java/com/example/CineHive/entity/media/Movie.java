package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Movie extends Media {
    
    // 영화 특화 필드
    private String originalTitle;
    private String originalLanguage;
    private boolean adult;
    
    // 기본 생성자
    public Movie(Long id) {
        super();
        setId(id);
        setMediaType(MediaType.MOVIE);
        setCategory(MediaCategory.DEFAULT);
    }
} 