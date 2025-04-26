package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tv_series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Tv extends Media {
    
    // TV 특화 필드
    private String originalName;
    private String originalLanguage;
    
    @ElementCollection
    @CollectionTable(name = "tv_origin_countries", joinColumns = @JoinColumn(name = "tv_id"))
    @Column(name = "origin_country")
    private List<String> originCountry = new ArrayList<>();
    
    private int numberOfSeasons;
    private int numberOfEpisodes;
    private String status; // 방영 중, 종영 등
    
    // 첫 방영일과 종영일
    @Column(name = "first_air_date") 
    private String firstAirDate;
    @Column(name = "last_air_date")
    private String lastAirDate;
    
    // 기본 생성자
    public Tv(Long id) {
        super();
        setId(id);
        setMediaType(MediaType.TV);
        setCategory(MediaCategory.DEFAULT);
    }
} 