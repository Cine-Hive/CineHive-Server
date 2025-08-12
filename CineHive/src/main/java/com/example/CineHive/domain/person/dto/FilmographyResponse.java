package com.example.CineHive.domain.person.dto;

import com.example.CineHive.client.tmdb.dto.TmdbPersonCreditResponse;
import com.example.CineHive.domain.media.enums.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

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
        LocalDate date = MediaType.MOVIE.getValue().equals(credit.mediaType().getValue()) ? credit.releaseDate() : credit.firstAirDate();
        Integer year = (date != null) ? date.getYear() : null;

        String title = MediaType.MOVIE.getValue().equals(credit.mediaType().getValue()) ? credit.title() : credit.name();

        return FilmographyResponse.builder()
                .mediaId(credit.id())
                .mediaType(credit.mediaType().getValue())
                .title(title)
                .posterPath(credit.posterPath())
                .releaseYear(year)
                .role(determineRole(credit))
                .build();
    }

    /**
     * [추가] Credit 객체에서 역할(배우/감독 등)을 결정하는 private 헬퍼 메서드
     */
    private static String determineRole(TmdbPersonCreditResponse credit) {
        if (credit.character() != null && !credit.character().isBlank()) {
            return credit.character();
        }
        if (credit.job() != null && !credit.job().isBlank()) {
            return credit.job();
        }
        return "N/A";
    }
}