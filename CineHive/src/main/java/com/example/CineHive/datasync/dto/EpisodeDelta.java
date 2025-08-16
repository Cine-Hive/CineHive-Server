package com.example.CineHive.datasync.dto;

import com.example.CineHive.client.tmdb.dto.TmdbEpisodeDetailResponse;
import com.example.CineHive.datasync.domain.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * 에피소드 동기화를 위한 Delta DTO
 * 에피소드 본체와 관련 데이터(출연진, 제작진, 이미지, 비디오)를 포함
 */
public record EpisodeDelta(
    Episode episode,
    List<EpisodeCast> cast,
    List<EpisodeCrew> crew,
    List<EpisodeImage> images,
    List<EpisodeVideo> videos
) {
    
    /**
     * TMDB API 응답을 EpisodeDelta로 변환하는 static factory 메서드
     */
    public static EpisodeDelta fromTmdbResponse(Long tvTmdbId, Long seasonTmdbId, 
                                               Integer seasonNumber, TmdbEpisodeDetailResponse response) {
        
        // 1. Episode 본체 생성
        Episode episode = Episode.builder()
                .tmdbId(response.id())
                .tvTmdbId(tvTmdbId)
                .seasonTmdbId(seasonTmdbId)
                .seasonNumber(seasonNumber)
                .episodeNumber(response.episodeNumber())
                .name(response.name())
                .overview(response.overview())
                .airDate(parseDate(response.airDate()))
                .runtime(response.runtime())
                .stillPath(response.stillPath())
                .voteAverage(response.voteAverage() != null ? 
                           new BigDecimal(response.voteAverage().toString()) : null)
                .voteCount(response.voteCount())
                .updatedFromTmdbAt(ZonedDateTime.now())
                .build();
        
        // 2. Cast 변환
        List<EpisodeCast> castList = response.credits() != null && response.credits().cast() != null ?
                response.credits().cast().stream()
                        .map(castMember -> EpisodeCast.builder()
                                .episodeId(response.id())
                                .personId(castMember.id())
                                .creditId(castMember.creditId())
                                .characterName(castMember.character())
                                .castOrder(castMember.order())
                                .isGuest(false)
                                .build())
                        .toList() : Collections.emptyList();
        
        // 3. Crew 변환
        List<EpisodeCrew> crewList = response.credits() != null && response.credits().crew() != null ?
                response.credits().crew().stream()
                        .map(crewMember -> EpisodeCrew.builder()
                                .episodeId(response.id())
                                .personId(crewMember.id())
                                .creditId(crewMember.creditId())
                                .department(crewMember.department())
                                .job(crewMember.job())
                                .build())
                        .toList() : Collections.emptyList();
        
        // 4. Images 변환 (에피소드는 backdrops를 stills로 사용)
        List<EpisodeImage> imageList = response.images() != null && response.images().backdrops() != null ?
                response.images().backdrops().stream()
                        .map(image -> EpisodeImage.builder()
                                .episodeTmdbId(response.id())
                                .filePath(image.filePath())
                                .width(image.width())
                                .height(image.height())
                                .aspectRatio(image.aspectRatio() != null ? 
                                           new BigDecimal(image.aspectRatio().toString()) : null)
                                .voteAverage(image.voteAverage() != null ? 
                                           new BigDecimal(image.voteAverage().toString()) : null)
                                .voteCount(image.voteCount())
                                .build())
                        .toList() : Collections.emptyList();
        
        // 5. Videos 변환
        List<EpisodeVideo> videoList = response.videos() != null && response.videos().results() != null ?
                response.videos().results().stream()
                        .map(video -> EpisodeVideo.builder()
                                .episodeTmdbId(response.id())
                                .videoKey(video.key())
                                .name(video.name())
                                .site(video.site())
                                .type(video.type())
                                .iso6391(video.iso6391())
                                .official(video.official())
                                .publishedAt(parseDateTime(video.publishedAt()))
                                .build())
                        .toList() : Collections.emptyList();
        
        return new EpisodeDelta(episode, castList, crewList, imageList, videoList);
    }
    
    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    private static ZonedDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        
        try {
            return ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                // Fallback to ISO_OFFSET_DATE_TIME if no zone ID
                return ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}