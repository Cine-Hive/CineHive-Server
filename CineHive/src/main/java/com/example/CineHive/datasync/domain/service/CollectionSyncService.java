package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbCollectionDetailResponse;
import com.example.CineHive.datasync.domain.entity.Collection;
import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.CollectionRepository;
import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import com.example.CineHive.global.exception.TmdbClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionSyncService {

    private final CollectionRepository collectionRepository;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;
    private final TmdbApiClient tmdbApiClient;

    /**
     * 배치 처리용 컬렉션 동기화 메소드
     * TMDB API 호출 + 큐 관리를 포함한 완전한 동기화
     */
    @Transactional
    public void syncCollectionFromQueue(TmdbWorkQueue queueItem) {
        Long collectionId = queueItem.getTmdbId();
        
        try {
            // 1. TMDB API 호출로 상세 정보 가져오기
            TmdbCollectionDetailResponse tmdbResponse = tmdbApiClient.getCollectionDetail(collectionId);
            
            // 2. Collection 엔티티로 변환
            Collection collection = Collection.builder()
                    .tmdbId(tmdbResponse.id())
                    .name(tmdbResponse.name())
                    .overview(tmdbResponse.overview())
                    .posterPath(tmdbResponse.posterPath())
                    .backdropPath(tmdbResponse.backdropPath())
                    .updatedFromTmdbAt(ZonedDateTime.now())
                    .build();
            
            // 3. 데이터베이스에 동기화
            syncCollection(collection);
            
            // 4. 큐 아이템을 처리 완료로 마킹
            queueItem.markAsProcessed();
            tmdbWorkQueueRepository.save(queueItem);
            
            log.debug("컬렉션 동기화 완료: collectionId={}", collectionId);
            
        } catch (TmdbClientException e) {
            handleTmdbApiError(queueItem, e, collectionId);
        } catch (Exception e) {
            handleSyncError(queueItem, e, collectionId);
        }
    }
    
    /**
     * Collection을 데이터베이스에 동기화하는 메소드
     */
    @Transactional
    public void syncCollection(Collection collection) {
        Long collectionId = collection.getTmdbId();
        
        log.info("컬렉션 동기화 시작: collectionId={}, name={}", collectionId, collection.getName());
        
        // Collection 엔티티 저장 (UPSERT)
        collectionRepository.save(collection);
        
        log.info("컬렉션 동기화 완료: collectionId={}", collectionId);
    }
    
    private void handleTmdbApiError(TmdbWorkQueue queueItem, TmdbClientException e, Long collectionId) {
        if (e.getStatus() == HttpStatus.NOT_FOUND) {
            // 404는 영구 스킵 처리
            queueItem.markAsProcessed();
            queueItem.setLastError("Collection not found in TMDB: " + collectionId);
            log.warn("컬렉션 ID {}를 TMDB에서 찾을 수 없어 스킵합니다.", collectionId);
        } else {
            // 기타 API 에러는 재시도 대상
            queueItem.markAsFailed("TMDB API Error: " + e.getMessage());
            log.error("컬렉션 {} TMDB API 호출 실패: {}", collectionId, e.getMessage());
        }
        tmdbWorkQueueRepository.save(queueItem);
    }
    
    private void handleSyncError(TmdbWorkQueue queueItem, Exception e, Long collectionId) {
        queueItem.markAsFailed("Sync Error: " + e.getMessage());
        tmdbWorkQueueRepository.save(queueItem);
        log.error("컬렉션 {} 동기화 중 오류 발생", collectionId, e);
    }
}