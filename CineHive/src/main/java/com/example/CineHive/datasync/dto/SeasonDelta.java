package com.example.CineHive.datasync.dto;

import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.datasync.domain.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.IntStream;

/**
 * TV 시즌 데이터 묶음을 전달하기 위한 데이터 캐리어(DTO).
 * Java Record를 사용하여 불변 객체로 간결하게 정의.
 */
@Slf4j
public record SeasonDelta(
        TvSeason season,
        List<Episode> episodes,
        List<TvSeasonCast> cast,
        List<TvSeasonCrew> crew,
        List<TvSeasonImage> images
) {
    
    /**
     * TMDB API 응답을 SeasonDelta로 변환하는 static factory 메서드
     */
    public static SeasonDelta fromTmdbResponse(Long tvTmdbId, TmdbSeasonDetailResponse response) {
        // TvSeason 엔티티 생성
        TvSeason season = TvSeason.builder()
                .tmdbId(response.id())
                .tvTmdbId(tvTmdbId)
                .seasonNumber(response.seasonNumber())
                .name(response.name())
                .overview(response.overview())
                .airDate(parseDate(response.airDate()))
                .episodeCount(response.episodes() != null ? response.episodes().size() : 0)
                .posterPath(response.posterPath())
                .voteAverage(response.voteAverage())
                .updatedFromTmdbAt(java.time.ZonedDateTime.now())
                .build();
        
        // Episodes 처리
        List<Episode> episodes = response.episodes() != null ?
            response.episodes().stream()
                .map(ep -> Episode.builder()
                    .tmdbId(ep.id())
                    .tvTmdbId(tvTmdbId)
                    .seasonTmdbId(response.id())
                    .seasonNumber(response.seasonNumber())
                    .episodeNumber(ep.episodeNumber())
                    .name(ep.name())
                    .overview(ep.overview())
                    .airDate(parseDate(ep.airDate()))
                    .runtime(ep.runtime())
                    .stillPath(ep.stillPath())
                    .voteAverage(ep.voteAverage())
                    .voteCount(ep.voteCount())
                    .updatedFromTmdbAt(java.time.ZonedDateTime.now())
                    .build())
                .toList() : List.of();
        
        // Cast 처리
        List<TvSeasonCast> cast = response.credits() != null && response.credits().cast() != null ?
            IntStream.range(0, response.credits().cast().size())
                .mapToObj(i -> TvSeasonCast.fromTmdbResponse(response.id(), 
                    response.credits().cast().get(i), i))
                .toList() : List.of();
        
        // Crew 처리
        List<TvSeasonCrew> crew = response.credits() != null && response.credits().crew() != null ?
            response.credits().crew().stream()
                .map(crewMember -> TvSeasonCrew.fromTmdbResponse(response.id(), crewMember))
                .toList() : List.of();
        
        // Images 처리
        List<TvSeasonImage> images = response.images() != null && response.images().posters() != null ?
            response.images().posters().stream()
                .map(img -> TvSeasonImage.fromTmdbResponse(response.id(), img))
                .toList() : List.of();
        
        return new SeasonDelta(season, episodes, cast, crew, images);
    }
    
    private static java.time.LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(dateString);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateString);
            return null;
        }
    }
}