package com.example.CineHive.datasync.dto;

import com.example.CineHive.client.tmdb.dto.TmdbPersonDetailResponse;
import com.example.CineHive.datasync.domain.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemProcessor에서 ItemWriter로 변환된 인물 데이터 묶음을 전달하기 위한 데이터 캐리어(DTO).
 * Java Record를 사용하여 불변 객체로 간결하게 정의.
 */
@Slf4j
public record PersonDelta(
        Person person,
        List<MovieCast> movieCast,
        List<MovieCrew> movieCrew,
        List<TvCast> tvCast,
        List<TvCrew> tvCrew
) {

    /**
     * TMDB API 응답을 PersonDelta로 변환하는 static factory 메서드
     */
    public static PersonDelta fromTmdbResponse(TmdbPersonDetailResponse response) {
        // Person 엔티티 생성 (updatedFromTmdbAt은 Builder에서 설정)
        Person person = Person.builder()
                .tmdbId(response.id())
                .name(response.name())
                .biography(response.biography())
                .birthday(response.birthday())
                .deathday(response.deathday())
                .gender(com.example.CineHive.datasync.domain.enums.GenderType.fromTmdbId(response.gender()))
                .profilePath(response.profilePath())
                .popularity(response.popularity())
                .updatedFromTmdbAt(ZonedDateTime.now())
                .build();
        
        List<MovieCast> movieCast = new ArrayList<>();
        List<MovieCrew> movieCrew = new ArrayList<>();
        
        // Movie Credits 처리
        if (response.movieCredits() != null) {
            if (response.movieCredits().cast() != null) {
                int castOrder = 0;
                for (var cast : response.movieCredits().cast()) {
                    movieCast.add(MovieCast.builder()
                            .creditId(cast.creditId())
                            .movieId(cast.id())
                            .personId(response.id())
                            .characterName(cast.character())
                            .castOrder(castOrder++)
                            .build());
                }
            }
            
            if (response.movieCredits().crew() != null) {
                for (var crew : response.movieCredits().crew()) {
                    movieCrew.add(MovieCrew.builder()
                            .creditId(crew.creditId())
                            .movieId(crew.id())
                            .personId(response.id())
                            .job(crew.job())
                            .department(crew.department())
                            .build());
                }
            }
        }
        
        List<TvCast> tvCast = new ArrayList<>();
        List<TvCrew> tvCrew = new ArrayList<>();
        
        // TV Credits 처리
        if (response.tvCredits() != null) {
            if (response.tvCredits().cast() != null) {
                int castOrder = 0;
                for (var cast : response.tvCredits().cast()) {
                    tvCast.add(TvCast.builder()
                            .creditId(cast.creditId())
                            .tvId(cast.id())
                            .personId(response.id())
                            .characterName(cast.character())
                            .castOrder(castOrder++)
                            .build());
                }
            }
            
            if (response.tvCredits().crew() != null) {
                for (var crew : response.tvCredits().crew()) {
                    tvCrew.add(TvCrew.builder()
                            .creditId(crew.creditId())
                            .tvId(crew.id())
                            .personId(response.id())
                            .job(crew.job())
                            .department(crew.department())
                            .build());
                }
            }
        }

        return new PersonDelta(person, movieCast, movieCrew, tvCast, tvCrew);
    }
}