package com.example.CineHive.dto.media;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private Long id;
    private String posterPath;
    private String title;
    private String releaseDate;
    private List<String> genres;
} 