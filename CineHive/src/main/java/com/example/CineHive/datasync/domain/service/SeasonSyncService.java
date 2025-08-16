package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbSeasonDetailResponse;
import com.example.CineHive.datasync.dto.SeasonDelta;
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
public class SeasonSyncService {

    private final TvSeasonRepository tvSeasonRepository;
    private final EpisodeRepository episodeRepository;
    private final TvSeasonCastRepository tvSeasonCastRepository;
    private final TvSeasonCrewRepository tvSeasonCrewRepository;
    private final TvSeasonImageRepository tvSeasonImageRepository;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;
    private final TmdbApiClient tmdbApiClient;

    /**
     * 배치 처리용 시즌 동기화 메소드
     * TMDB API 호출 + 큐 관리를 포함한 완전한 동기화
     */
    @Transactional
    public void syncSeasonFromQueue(Long tvTmdbId, TmdbWorkQueue queueItem) {
        Long seasonId = queueItem.getTmdbId();
        
        try {
            // 시즌 번호는 큐 아이템의 메타데이터에서 가져와야 함
            Integer seasonNumber = extractSeasonNumber(queueItem);
            
            // 1. TMDB API 호출로 상세 정보 가져오기
            TmdbSeasonDetailResponse tmdbResponse = tmdbApiClient.getSeasonDetail(tvTmdbId, seasonNumber);
            
            // 2. TMDB 응답을 SeasonDelta로 변환
            SeasonDelta delta = SeasonDelta.fromTmdbResponse(tvTmdbId, tmdbResponse);
            
            // 3. 데이터베이스에 동기화
            syncSeason(delta);
            
            // 4. 큐 아이템을 처리 완료로 마킹
            queueItem.markAsProcessed();
            tmdbWorkQueueRepository.save(queueItem);
            
            log.debug("시즌 동기화 완료: tvId={}, seasonNumber={}", tvTmdbId, seasonNumber);
            
        } catch (TmdbClientException e) {
            handleTmdbApiError(queueItem, e, seasonId);
        } catch (Exception e) {
            handleSyncError(queueItem, e, seasonId);
        }
    }
    
    /**
     * SeasonDelta를 받아서 데이터베이스에 동기화하는 메소드
     */
    @Transactional
    public void syncSeason(SeasonDelta delta) {
        Long seasonId = delta.season().getTmdbId();

        // 1. TvSeason 본체 엔티티 저장 (UPSERT)
        tvSeasonRepository.save(delta.season());

        // 2. Episodes 저장
        if (delta.episodes() != null && !delta.episodes().isEmpty()) {
            episodeRepository.saveAll(delta.episodes());
            log.info("{}개 에피소드 저장 완료: seasonId={}", delta.episodes().size(), seasonId);
        }

        // 3. 관계 데이터는 기존 것을 모두 지우고 새로 삽입
        tvSeasonCastRepository.deleteAllBySeasonId(seasonId);
        if (delta.cast() != null && !delta.cast().isEmpty()) {
            tvSeasonCastRepository.saveAll(delta.cast());
        }

        tvSeasonCrewRepository.deleteAllBySeasonId(seasonId);
        if (delta.crew() != null && !delta.crew().isEmpty()) {
            tvSeasonCrewRepository.saveAll(delta.crew());
        }

        // 4. 이미지 저장 (기존 삭제 후 새로 삽입)
        tvSeasonImageRepository.deleteAllBySeasonId(seasonId);
        if (delta.images() != null && !delta.images().isEmpty()) {
            tvSeasonImageRepository.saveAll(delta.images());
        }

        log.info("시즌 동기화 완료: seasonId={}, name={}", seasonId, delta.season().getName());
    }
    
    private Integer extractSeasonNumber(TmdbWorkQueue queueItem) {
        // 메타데이터나 추가 정보에서 시즌 번호를 추출
        // 실제 구현은 큐 아이템의 구조에 따라 달라질 수 있음
        if (queueItem.getLastError() != null && queueItem.getLastError().contains("season:")) {
            String[] parts = queueItem.getLastError().split("season:");
            if (parts.length > 1) {
                return Integer.parseInt(parts[1].trim());
            }
        }
        // 기본값 또는 예외 처리
        return 1;
    }
    
    private void handleTmdbApiError(TmdbWorkQueue queueItem, TmdbClientException e, Long seasonId) {
        if (e.getStatus() == HttpStatus.NOT_FOUND) {
            // 404는 영구 스킵 처리
            queueItem.markAsProcessed();
            queueItem.setLastError("Season not found in TMDB: " + seasonId);
            log.warn("시즌 ID {}를 TMDB에서 찾을 수 없어 스킵합니다.", seasonId);
        } else {
            // 기타 API 에러는 재시도 대상
            queueItem.markAsFailed("TMDB API Error: " + e.getMessage());
            log.error("시즌 {} TMDB API 호출 실패: {}", seasonId, e.getMessage());
        }
        tmdbWorkQueueRepository.save(queueItem);
    }
    
    private void handleSyncError(TmdbWorkQueue queueItem, Exception e, Long seasonId) {
        queueItem.markAsFailed("Sync Error: " + e.getMessage());
        tmdbWorkQueueRepository.save(queueItem);
        log.error("시즌 {} 동기화 중 오류 발생", seasonId, e);
    }
}