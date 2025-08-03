package com.example.CineHive.domain.media.controller.entity;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.domain.review.dto.RatingStats;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

/**
 * MediaService의 구현 클래스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final TmdbApiClient tmdbApiClient;

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "media", key = "#tmdbId + '_' + #mediaType.name()", unless = "#result == null")
    @Transactional
    public Media findOrCreateMedia(Integer tmdbId, MediaType mediaType) {
        return mediaRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                .orElseGet(() -> {
                    try {
                        log.debug("Cache miss. DB에 미디어 정보가 없어 TMDB API를 통해 새로 생성합니다. tmdbId: {}, mediaType: {}", tmdbId, mediaType);
                        Media newMedia = createMediaFromTmdb(tmdbId, mediaType);
                        return mediaRepository.save(newMedia);
                    } catch (DataIntegrityViolationException e) {
                        log.warn("Race condition 발생: 미디어 동시 생성 시도. tmdbId: {}, mediaType: {}. 재조회합니다.", tmdbId, mediaType);
                        return mediaRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)); // 이 경우는 발생하면 안 되는 심각한 오류
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateMediaRating(Media media, RatingStats stats) {
        media.updateRating(stats.average(), (int) stats.count());
        log.debug("미디어 평점이 업데이트되었습니다. mediaId: {}, averageRating: {}, reviewCount: {}", media.getId(), stats.average(), stats.count());
    }

    /**
     * TMDB API를 호출하여 미디어 상세 정보를 바탕으로 Media 엔티티를 생성합니다.
     */
    private Media createMediaFromTmdb(Integer tmdbId, MediaType mediaType) {
        try {
            if (mediaType.isMovie()) {
                var tmdbMovie = tmdbApiClient.getMovieDetail(tmdbId.longValue());
                return Media.from(tmdbMovie);
            } else {
                var tmdbTv = tmdbApiClient.getTvSeriesDetail(tmdbId.longValue());
                return Media.from(tmdbTv);
            }
        } catch (RestClientException e) {
            log.error("TMDB API 호출 중 에러가 발생했습니다. tmdbId: {}", tmdbId, e);
            throw new BusinessException(ErrorCode.TMDB_API_ERROR, e);
        }
    }
}