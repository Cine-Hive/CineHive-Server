package com.example.CineHive.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "미디어 아이템 상세 정보")
public class MediaItemDto {
    @Schema(description = "미디어 ID", example = "550")
    private Long id;
    
    @Schema(description = "미디어 제목", example = "파이트 클럽")
    private String title;
    
    @Schema(description = "미디어 줄거리", example = "외로움과 불면증에 시달리는 젊은 회사원은 타일러 더든이라는 수상한 비누 제조업자를 만나게 되고...")
    private String overview;
    
    @Schema(description = "포스터 이미지 경로", example = "/path/to/poster.jpg")
    private String posterPath;
    
    @Schema(description = "배경 이미지 경로", example = "/path/to/backdrop.jpg")
    private String backdropPath;
    
    @Schema(description = "개봉일 또는 방영일", example = "1999-10-15")
    private String releaseDate;
    
    @Schema(description = "장르 목록")
    private List<GenreDto> genres;
    
    @Schema(description = "장르 ID 목록", example = "[18, 53, 35]")
    private List<Integer> genreIds;
    
    @Schema(description = "평점", example = "8.4")
    private double voteAverage;
    
    @Schema(description = "인기도", example = "42.568")
    private double popularity;
    
    @Schema(description = "미디어 타입", example = "movie", allowableValues = {"movie", "tv", "animation"})
    private String mediaType;
    
    @Schema(description = "카테고리", example = "popular", allowableValues = {"popular", "top_rated", "now_playing", "upcoming", "similar"})
    private String category; 
    
    @Schema(description = "제작/방영 국가 코드", example = "[\"US\", \"UK\"]")
    private List<String> originCountry;
    
    @Schema(description = "원본 언어", example = "en")
    private String originalLanguage;
    
    @Schema(description = "원제목", example = "Fight Club")
    private String originalTitle;
    
    @Schema(description = "시즌 수 (TV 시리즈용)", example = "5")
    private Integer numberOfSeasons;
    
    @Schema(description = "에피소드 수 (TV 시리즈용)", example = "62")
    private Integer numberOfEpisodes;
    
    @Schema(description = "첫 방영일 (TV 시리즈용)", example = "2008-01-20")
    private String firstAirDate;
    
    @Schema(description = "마지막 방영일 (TV 시리즈용)", example = "2013-09-29")
    private String lastAirDate;
    
    @Schema(description = "비디오 정보 목록")
    private List<VideoDto> videos;
    
    @Schema(description = "출연진 목록")
    private List<CastDto> cast;
    
    @Schema(description = "제작진 목록")
    private List<CrewDto> crew;
    
    @Schema(description = "추천 미디어 목록")
    private List<MediaItemDto> recommendations;
    
    @Schema(description = "상영 시간 (분)", example = "139")
    private Integer runtime;
} 