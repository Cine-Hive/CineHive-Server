package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Media {
    @Id
    private Long id;
    
    private String title; // 영화, 드라마, 애니메이션의 제목
    
    @Lob
    private String overview;
    
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    private double voteAverage;
    private double popularity;
    
    private int runtime;
    
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    
    // 장르는 각 서브클래스에서 구현 (Movie, Drama, Animation)
    
    public enum MediaType {
        MOVIE, TV, ANIMATION
    }
    
    @Enumerated(EnumType.STRING)
    private MediaCategory category;
    
    public enum MediaCategory {
        POPULAR, TOP_RATED, NOW_PLAYING, UPCOMING, ON_THE_AIR, AIRING_TODAY, DEFAULT
    }
} 