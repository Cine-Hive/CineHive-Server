package com.example.CineHive.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaDetailDto {
    private Long id;
    private String title;
    private String originalTitle;
    private String overview;
    private LocalDate releaseDate;
    private String posterPath;
    private String backdropPath;
    private Double voteAverage;
    private Integer voteCount;
    private Double popularity;
    private boolean isAnimation;
    private List<GenreDto> genres;
    private List<CastDto> cast;
    private List<DirectorDto> directors;
    private List<VideoDto> videos;
    private List<ImageDto> images;
    private List<MediaSummaryDto> recommendations;
    private List<MediaSummaryDto> similar;
    private List<KeywordDto> keywords;
    private WatchProvidersDto watchProviders;
}