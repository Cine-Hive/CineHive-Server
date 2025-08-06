package com.example.CineHive.domain.search.dto;

import com.example.CineHive.domain.search.document.MediaDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "미디어 검색 결과 항목 응답 DTO")
public record MediaSearchResponse(
        @Schema(description = "TMDB 미디어 ID")
        Long tmdbId,
        @Schema(description = "미디어 타입 (MOVIE 또는 TV)")
        String mediaType,
        @Schema(description = "제목")
        String title,
        @Schema(description = "포스터 이미지 경로")
        String posterPath,
        @Schema(description = "개봉일")
        String releaseDate,
        @Schema(description = "CineHive 좋아요 수")
        int likeCount
) {
    /**
     * MediaDocument를 MediaSearchResponse DTO로 변환합니다.
     */
    public static MediaSearchResponse from(MediaDocument document) {
        return MediaSearchResponse.builder()
                .tmdbId(document.getTmdbId())
                .mediaType(document.getMediaType())
                .title(document.getTitle())
                .posterPath(document.getPosterPath())
                .releaseDate(document.getReleaseDate())
                .likeCount(document.getLikeCount())
                .build();
    }
}