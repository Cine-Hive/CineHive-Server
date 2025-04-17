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
@Schema(description = "미디어 목록 응답 DTO")
public class MediaDto {
    @Schema(description = "미디어 아이템 목록", example = "[]")
    private List<MediaItemDto> results;
    
    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;
    
    @Schema(description = "전체 페이지 수", example = "100")
    private int totalPages;
    
    @Schema(description = "전체 결과 수", example = "2000")
    private int totalResults;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "미디어 아이템 상세 정보")
    public static class MediaItemDto {
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
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "장르 정보")
    public static class GenreDto {
        @Schema(description = "장르 ID", example = "18")
        private Integer id;
        
        @Schema(description = "장르명", example = "드라마")
        private String name;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "비디오 정보")
    public static class VideoDto {
        @Schema(description = "비디오 ID", example = "5c9294240e0a267cd516835f")
        private String id;
        
        @Schema(description = "비디오 제목", example = "파이트 클럽 공식 예고편")
        private String name;
        
        @Schema(description = "비디오 키", example = "BdJKm16Co6M")
        private String key;
        
        @Schema(description = "비디오 사이트", example = "YouTube")
        private String site;
        
        @Schema(description = "비디오 유형", example = "Trailer")
        private String type;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "출연진 정보")
    public static class CastDto {
        @Schema(description = "인물 ID", example = "819")
        private Long id;
        
        @Schema(description = "출연진 ID", example = "52fe4250c3a36847f8014a11")
        private Long castId;
        
        @Schema(description = "배우 이름", example = "브래드 피트")
        private String name;
        
        @Schema(description = "배역", example = "타일러 더든")
        private String character;
        
        @Schema(description = "프로필 이미지 경로", example = "/path/to/profile.jpg")
        private String profilePath;
        
        @Schema(description = "순서", example = "1")
        private Integer order;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "제작진 정보")
    public static class CrewDto {
        @Schema(description = "인물 ID", example = "7467")
        private Long id;
        
        @Schema(description = "제작진 이름", example = "데이비드 핀처")
        private String name;
        
        @Schema(description = "직무", example = "Director")
        private String job;
        
        @Schema(description = "부서", example = "Directing")
        private String department;
        
        @Schema(description = "프로필 이미지 경로", example = "/path/to/profile.jpg")
        private String profilePath;
    }
} 