package com.example.CineHive.domain.person.dto;

import com.example.CineHive.client.tmdb.dto.TmdbPersonInListResponse;
import com.example.CineHive.domain.person.entity.Person;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "인물 목록의 각 항목에 대한 정보 응답 DTO")
public record PersonInListResponse(
        @Schema(description = "CineHive 내부 인물 ID (DB에 없을 경우 null)", nullable = true)
        Long id,

        @Schema(description = "TMDB 인물 고유 ID")
        Long tmdbId,

        @Schema(description = "인물 이름")
        String name,

        @Schema(description = "프로필 이미지 경로")
        String profilePath
) {
    /**
     * Person 엔티티를 DTO로 변환합니다.
     */
    public static PersonInListResponse from(Person person) {
        return PersonInListResponse.builder()
                .id(person.getId())
                .tmdbId(person.getTmdbId())
                .name(person.getName())
                .profilePath(person.getProfilePath())
                .build();
    }

    /**
     * TMDB API 응답 DTO를 우리 서비스의 응답 DTO로 변환합니다.
     * 이 경우 우리 DB에 해당 인물이 없을 수 있으므로, 내부 id는 null이 됩니다.
     */
    public static PersonInListResponse from(TmdbPersonInListResponse tmdbPerson) {
        return PersonInListResponse.builder()
                .id(null)
                .tmdbId(tmdbPerson.id())
                .name(tmdbPerson.name())
                .profilePath(tmdbPerson.profilePath())
                .build();
    }
}