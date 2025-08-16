package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbEpisodeDetailResponse;
import com.example.CineHive.datasync.dto.EpisodeDelta;
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
public class EpisodeSyncService {

    private final EpisodeRepository episodeRepository;
    private final EpisodeCastRepository episodeCastRepository;
    private final EpisodeCrewRepository episodeCrewRepository;
    private final EpisodeImageRepository episodeImageRepository;
    private final EpisodeVideoRepository episodeVideoRepository;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;
    private final TmdbApiClient tmdbApiClient;

    /**
     * 배치 처리용 에피소드 동기화 메소드
     * TMDB API 호출 + 큐 관리를 포함한 완전한 동기화
     */
    @Transactional
    public void syncEpisodeFromQueue(Long tvTmdbId, TmdbWorkQueue queueItem) {
        Long episodeId = queueItem.getTmdbId();
        
        try {
            // 시즌 번호와 에피소드 번호는 큐 아이템의 메타데이터에서 추출
            EpisodeMetadata metadata = extractEpisodeMetadata(queueItem);
            
            // 1. TMDB API 호출로 상세 정보 가져오기
            TmdbEpisodeDetailResponse tmdbResponse = tmdbApiClient.getEpisodeDetail(
                tvTmdbId, metadata.seasonNumber(), metadata.episodeNumber()
            );
            
            // 2. TMDB 응답을 EpisodeDelta로 변환
            EpisodeDelta delta = EpisodeDelta.fromTmdbResponse(
                tvTmdbId, metadata.seasonTmdbId(), metadata.seasonNumber(), tmdbResponse
            );
            
            // 3. 데이터베이스에 동기화
            syncEpisode(delta);
            
            // 4. 큐 아이템을 처리 완료로 마킹
            queueItem.markAsProcessed();
            tmdbWorkQueueRepository.save(queueItem);
            
            log.debug("에피소드 동기화 완료: tvId={}, season={}, episode={}", 
                     tvTmdbId, metadata.seasonNumber(), metadata.episodeNumber());
            
        } catch (TmdbClientException e) {
            handleTmdbApiError(queueItem, e, episodeId);
        } catch (Exception e) {
            handleSyncError(queueItem, e, episodeId);
        }
    }
    
    /**
     * EpisodeDelta를 받아서 데이터베이스에 동기화하는 메소드
     */
    @Transactional
    public void syncEpisode(EpisodeDelta delta) {
        Long episodeId = delta.episode().getTmdbId();

        // 1. Episode 본체 엔티티 저장 (UPSERT)
        episodeRepository.save(delta.episode());

        // 2. 관계 데이터는 기존 것을 모두 지우고 새로 삽입
        
        // Cast
        episodeCastRepository.deleteAllByEpisodeId(episodeId);
        if (delta.cast() != null && !delta.cast().isEmpty()) {
            episodeCastRepository.saveAll(delta.cast());
            log.info("{}명 출연진 저장 완료: episodeId={}", delta.cast().size(), episodeId);
        }

        // Crew
        episodeCrewRepository.deleteAllByEpisodeId(episodeId);
        if (delta.crew() != null && !delta.crew().isEmpty()) {
            episodeCrewRepository.saveAll(delta.crew());
            log.info("{}명 제작진 저장 완료: episodeId={}", delta.crew().size(), episodeId);
        }

        // Images
        episodeImageRepository.deleteAllByEpisodeId(episodeId);
        if (delta.images() != null && !delta.images().isEmpty()) {
            episodeImageRepository.saveAll(delta.images());
            log.info("{}개 이미지 저장 완료: episodeId={}", delta.images().size(), episodeId);
        }

        // Videos
        episodeVideoRepository.deleteAllByEpisodeId(episodeId);
        if (delta.videos() != null && !delta.videos().isEmpty()) {
            episodeVideoRepository.saveAll(delta.videos());
            log.info("{}개 비디오 저장 완료: episodeId={}", delta.videos().size(), episodeId);
        }

        log.info("에피소드 동기화 완료: episodeId={}, name={}", episodeId, delta.episode().getName());
    }
    
    /**
     * 큐 아이템에서 에피소드 메타데이터를 추출하는 메소드
     */
    private EpisodeMetadata extractEpisodeMetadata(TmdbWorkQueue queueItem) {
        // 메타데이터 형식: "tv:12345,season:67890,sn:1,ep:5"
        // tv: TV 시리즈 ID, season: 시즌 TMDB ID, sn: 시즌 번호, ep: 에피소드 번호
        String metadata = queueItem.getLastError();
        if (metadata != null && metadata.contains("sn:") && metadata.contains("ep:")) {
            try {
                String[] parts = metadata.split(",");
                Long seasonTmdbId = null;
                Integer seasonNumber = null;
                Integer episodeNumber = null;
                
                for (String part : parts) {
                    if (part.startsWith("season:")) {
                        seasonTmdbId = Long.parseLong(part.substring(7));
                    } else if (part.startsWith("sn:")) {
                        seasonNumber = Integer.parseInt(part.substring(3));
                    } else if (part.startsWith("ep:")) {
                        episodeNumber = Integer.parseInt(part.substring(3));
                    }
                }
                
                if (seasonTmdbId != null && seasonNumber != null && episodeNumber != null) {
                    return new EpisodeMetadata(seasonTmdbId, seasonNumber, episodeNumber);
                }
            } catch (Exception e) {
                log.warn("에피소드 메타데이터 파싱 실패: {}", metadata, e);
            }
        }
        
        // 기본값 반환 (실제 운영에서는 예외 처리 필요)
        return new EpisodeMetadata(0L, 1, 1);
    }
    
    private void handleTmdbApiError(TmdbWorkQueue queueItem, TmdbClientException e, Long episodeId) {
        if (e.getStatus() == HttpStatus.NOT_FOUND) {
            // 404는 영구 스킵 처리
            queueItem.markAsProcessed();
            queueItem.setLastError("Episode not found in TMDB: " + episodeId);
            log.warn("에피소드 ID {}를 TMDB에서 찾을 수 없어 스킵합니다.", episodeId);
        } else {
            // 기타 API 에러는 재시도 대상
            queueItem.markAsFailed("TMDB API Error: " + e.getMessage());
            log.error("에피소드 {} TMDB API 호출 실패: {}", episodeId, e.getMessage());
        }
        tmdbWorkQueueRepository.save(queueItem);
    }
    
    private void handleSyncError(TmdbWorkQueue queueItem, Exception e, Long episodeId) {
        queueItem.markAsFailed("Sync Error: " + e.getMessage());
        tmdbWorkQueueRepository.save(queueItem);
        log.error("에피소드 {} 동기화 중 오류 발생", episodeId, e);
    }
    
    /**
     * 에피소드 메타데이터를 담는 내부 레코드 클래스
     */
    private record EpisodeMetadata(Long seasonTmdbId, Integer seasonNumber, Integer episodeNumber) {}
}