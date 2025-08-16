package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbTvSeriesDetailResponse;
import com.example.CineHive.datasync.dto.TvDelta;
import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.*;
import com.example.CineHive.global.exception.TmdbClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TvSyncService {

    // TV 시리즈와 관련된 모든 리포지토리 주입
    private final TvSeriesRepository tvSeriesRepository;
    private final NetworkRepository networkRepository;
    private final ProductionCompanyRepository productionCompanyRepository;
    private final GenreRepository genreRepository;
    private final KeywordRepository keywordRepository;
    private final TvGenreRepository tvGenreRepository;
    private final TvKeywordRepository tvKeywordRepository;
    private final TvCastRepository tvCastRepository;
    private final TvCrewRepository tvCrewRepository;
    private final TvSeriesNetworkRepository tvSeriesNetworkRepository;
    private final TvSeasonRepository tvSeasonRepository;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;
    
    // TMDB API 클라이언트
    private final TmdbApiClient tmdbApiClient;

    /**
     * 배치 처리용 TV 시리즈 동기화 메소드
     * TMDB API 호출 + 큐 관리를 포함한 완전한 동기화
     */
    @Transactional
    public void syncTvFromQueue(TmdbWorkQueue queueItem) {
        Long tvId = queueItem.getTmdbId();
        
        try {
            // 1. TMDB API 호출로 상세 정보 가져오기
            TmdbTvSeriesDetailResponse tmdbResponse = tmdbApiClient.getTvDetailForBatch(tvId);
            
            // 2. TMDB 응답을 TvDelta로 변환
            TvDelta delta = TvDelta.fromTmdbResponse(tmdbResponse);
            
            // 3. 데이터베이스에 동기화
            syncTv(delta);
            
            // 4. 큐 아이템을 처리 완료로 마킹
            queueItem.markAsProcessed();
            tmdbWorkQueueRepository.save(queueItem);
            
            log.debug("TV 시리즈 동기화 완료: tvId={}", tvId);
            
        } catch (TmdbClientException e) {
            handleTmdbApiError(queueItem, e, tvId);
        } catch (Exception e) {
            handleSyncError(queueItem, e, tvId);
        }
    }
    
    /**
     * TvDelta를 받아서 데이터베이스에 동기화하는 메소드
     * 기존 로직 유지 (delete-insert 패턴)
     */
    @Transactional
    public void syncTv(TvDelta delta) {
        Long tvId = delta.tvSeries().getTmdbId();

        // 1. 독립적인 엔티티들 먼저 저장 (UPSERT)
        // Genre와 Keyword 정보를 먼저 저장 (외래 키 제약 조건 해결)
        if (delta.genreEntities() != null && !delta.genreEntities().isEmpty()) {
            log.info("Saving {} genres for TV series {}", delta.genreEntities().size(), tvId);
            genreRepository.saveAll(delta.genreEntities());
            log.info("Genres saved successfully for TV series {}", tvId);
        }
        if (delta.keywordEntities() != null && !delta.keywordEntities().isEmpty()) {
            log.info("Saving {} keywords for TV series {}", delta.keywordEntities().size(), tvId);
            keywordRepository.saveAll(delta.keywordEntities());
            log.info("Keywords saved successfully for TV series {}", tvId);
        }
        
        // Network 정보가 있으면 저장
        if (delta.networks() != null && !delta.networks().isEmpty()) {
            networkRepository.saveAll(delta.networks());
        }
        // ProductionCompany 정보가 있으면 모두 저장
        if (delta.companies() != null && !delta.companies().isEmpty()) {
            productionCompanyRepository.saveAll(delta.companies());
        }

        // 2. TvSeries 본체 엔티티 저장 (UPSERT)
        tvSeriesRepository.save(delta.tvSeries());

        // 3. 관계 데이터는 기존 것을 모두 지우고 새로 삽입 (Full Sync에서의 멱등성 보장)
        tvGenreRepository.deleteAllByTvId(tvId);
        if (delta.genres() != null && !delta.genres().isEmpty()) {
            tvGenreRepository.saveAll(delta.genres());
        }

        tvKeywordRepository.deleteAllByTvId(tvId);
        if (delta.keywords() != null && !delta.keywords().isEmpty()) {
            tvKeywordRepository.saveAll(delta.keywords());
        }

        tvCastRepository.deleteAllByTvId(tvId);
        if (delta.cast() != null && !delta.cast().isEmpty()) {
            tvCastRepository.saveAll(delta.cast());
        }

        tvCrewRepository.deleteAllByTvId(tvId);
        if (delta.crew() != null && !delta.crew().isEmpty()) {
            tvCrewRepository.saveAll(delta.crew());
        }

        tvSeriesNetworkRepository.deleteAllByTvId(tvId);
        if (delta.tvNetworks() != null && !delta.tvNetworks().isEmpty()) {
            tvSeriesNetworkRepository.saveAll(delta.tvNetworks());
        }
        
        // 시즌 정보 저장 (시즌은 soft delete 처리가 더 적합할 수 있음)
        if (delta.seasons() != null && !delta.seasons().isEmpty()) {
            tvSeasonRepository.saveAll(delta.seasons());
        }

        // 4. Outbox 패턴을 이용한 캐시 무효화 이벤트 기록 (추후 구현)
    }
    
    private void handleTmdbApiError(TmdbWorkQueue queueItem, TmdbClientException e, Long tvId) {
        if (e.getStatus() == HttpStatus.NOT_FOUND) {
            // 404는 영구 스킵 처리
            queueItem.markAsProcessed();
            queueItem.setLastError("TV series not found in TMDB: " + tvId);
            log.warn("TV 시리즈 ID {}를 TMDB에서 찾을 수 없어 스킵합니다.", tvId);
        } else {
            // 기타 API 에러는 재시도 대상
            queueItem.markAsFailed("TMDB API Error: " + e.getMessage());
            log.error("TV 시리즈 {} TMDB API 호출 실패: {}", tvId, e.getMessage());
        }
        tmdbWorkQueueRepository.save(queueItem);
    }
    
    private void handleSyncError(TmdbWorkQueue queueItem, Exception e, Long tvId) {
        queueItem.markAsFailed("Sync Error: " + e.getMessage());
        tmdbWorkQueueRepository.save(queueItem);
        log.error("TV 시리즈 {} 동기화 중 오류 발생", tvId, e);
    }
}