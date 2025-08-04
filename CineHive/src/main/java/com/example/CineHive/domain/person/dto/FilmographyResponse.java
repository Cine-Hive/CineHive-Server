package com.example.CineHive.domain.person.dto;

import com.example.CineHive.client.tmdb.dto.TmdbPersonCreditResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "필모그래피 목록의 각 작품 정보")
public record FilmographyResponse(
        @Schema(description = "미디어의 TMDB 고유 ID")
        Long mediaId,
        @Schema(description = "미디어 타입 (movie 또는 tv)")
        String mediaType,
        @Schema(description = "작품 제목")
        String title,
        @Schema(description = "포스터 이미지 경로")
        String posterPath,
        @Schema(description = "개봉/방영 연도")
        Integer releaseYear,
        @Schema(description = "역할 (배역 또는 직책)")
        String role
) {
    /**
     * TMDB API의 Credit 객체를 우리 서비스의 응답 DTO로 변환합니다.
     */
    public static FilmographyResponse from(TmdbPersonCreditResponse credit) {
        String role = credit.character() != null && !credit.character().isEmpty()
                ? credit.character()
                : credit.job();

        Integer year = credit.getReleaseDate() != null
                ? credit.getReleaseDate().getYear()
                : null;

        return FilmographyResponse.builder()
                .mediaId(credit.id())
                .mediaType(credit.mediaType())
                .title(credit.getTitle())
                .posterPath(credit.posterPath())
                .releaseYear(year)
                .role(role)
                .build();
    }
}