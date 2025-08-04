package com.example.CineHive.domain.person.dto;

import com.example.CineHive.domain.person.entity.Person;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "인물 상세 정보 응답 DTO")
public record PersonDetailsResponse(
        @Schema(description = "CineHive 내부 인물 ID")
        Long id,

        @Schema(description = "TMDB 인물 고유 ID")
        Long tmdbId,

        @Schema(description = "인물 이름")
        String name,

        @Schema(description = "프로필 이미지 경로")
        String profilePath

        // TODO: 추후 인물 상세 정보 API 연동 시, 아래와 같은 필드 추가 가능
        /*
        @Schema(description = "성별")
        String gender,

        @Schema(description = "인물 소개")
        String biography,

        @Schema(description = "생년월일")
        java.time.LocalDate birthday
        */
) {
    /**
     * Person 엔티티를 PersonDetailsResponse DTO로 변환하는 정적 팩토리 메서드입니다.
     * @param person Person 엔티티
     * @return PersonDetailsResponse DTO
     */
    public static PersonDetailsResponse from(Person person) {
        return PersonDetailsResponse.builder()
                .id(person.getId())
                .tmdbId(person.getTmdbId())
                .name(person.getName())
                .profilePath(person.getProfilePath())
                .build();
    }
}