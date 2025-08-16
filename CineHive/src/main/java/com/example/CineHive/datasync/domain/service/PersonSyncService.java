package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbPersonDetailResponse;
import com.example.CineHive.datasync.dto.PersonDelta;
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
public class PersonSyncService {

    // 인물과 관련된 모든 리포지토리 주입
    private final PersonRepository personRepository;
    private final MovieCastRepository movieCastRepository;
    private final MovieCrewRepository movieCrewRepository;
    private final TvCastRepository tvCastRepository;
    private final TvCrewRepository tvCrewRepository;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;
    
    // TMDB API 클라이언트
    private final TmdbApiClient tmdbApiClient;

    /**
     * 배치 처리용 인물 동기화 메소드
     * TMDB API 호출 + 큐 관리를 포함한 완전한 동기화
     */
    @Transactional
    public void syncPersonFromQueue(TmdbWorkQueue queueItem) {
        Long personId = queueItem.getTmdbId();
        
        try {
            // 1. TMDB API 호출로 상세 정보 가져오기
            TmdbPersonDetailResponse tmdbResponse = tmdbApiClient.getPersonDetailForBatch(personId);
            
            // 2. TMDB 응답을 PersonDelta로 변환
            PersonDelta delta = PersonDelta.fromTmdbResponse(tmdbResponse);
            
            // 3. 데이터베이스에 동기화
            syncPerson(delta);
            
            // 4. 큐 아이템을 처리 완료로 마킹
            queueItem.markAsProcessed();
            tmdbWorkQueueRepository.save(queueItem);
            
            log.debug("인물 동기화 완료: personId={}", personId);
            
        } catch (TmdbClientException e) {
            handleTmdbApiError(queueItem, e, personId);
        } catch (Exception e) {
            handleSyncError(queueItem, e, personId);
        }
    }
    
    /**
     * PersonDelta를 받아서 데이터베이스에 동기화하는 메소드
     * 인물의 경우 크레딧 정보는 영화/TV 동기화 시에 주로 업데이트되므로
     * 여기서는 인물 기본 정보만 업데이트
     */
    @Transactional
    public void syncPerson(PersonDelta delta) {
        Long personId = delta.person().getTmdbId();

        // 1. Person 본체 엔티티 저장 (UPSERT)
        personRepository.save(delta.person());
        
        // 2. 크레딧 정보는 영화/TV 동기화 시에 업데이트되므로
        // 여기서는 Person API에서 제공하는 크레딧 정보는 저장하지 않음
        // (필요 시 아래 주석 해제)
        
        /*
        // Movie Credits 저장 (기존 데이터 삭제 후 새로 삽입)
        movieCastRepository.deleteAllByPersonId(personId);
        if (delta.movieCast() != null && !delta.movieCast().isEmpty()) {
            movieCastRepository.saveAll(delta.movieCast());
        }
        
        movieCrewRepository.deleteAllByPersonId(personId);
        if (delta.movieCrew() != null && !delta.movieCrew().isEmpty()) {
            movieCrewRepository.saveAll(delta.movieCrew());
        }
        
        // TV Credits 저장 (기존 데이터 삭제 후 새로 삽입)
        tvCastRepository.deleteAllByPersonId(personId);
        if (delta.tvCast() != null && !delta.tvCast().isEmpty()) {
            tvCastRepository.saveAll(delta.tvCast());
        }
        
        tvCrewRepository.deleteAllByPersonId(personId);
        if (delta.tvCrew() != null && !delta.tvCrew().isEmpty()) {
            tvCrewRepository.saveAll(delta.tvCrew());
        }
        */
        
        log.info("인물 정보 저장 완료: personId={}, name={}", personId, delta.person().getName());

        // 3. Outbox 패턴을 이용한 캐시 무효화 이벤트 기록 (추후 구현)
    }
    
    private void handleTmdbApiError(TmdbWorkQueue queueItem, TmdbClientException e, Long personId) {
        if (e.getStatus() == HttpStatus.NOT_FOUND) {
            // 404는 영구 스킵 처리
            queueItem.markAsProcessed();
            queueItem.setLastError("Person not found in TMDB: " + personId);
            log.warn("인물 ID {}를 TMDB에서 찾을 수 없어 스킵합니다.", personId);
        } else {
            // 기타 API 에러는 재시도 대상
            queueItem.markAsFailed("TMDB API Error: " + e.getMessage());
            log.error("인물 {} TMDB API 호출 실패: {}", personId, e.getMessage());
        }
        tmdbWorkQueueRepository.save(queueItem);
    }
    
    private void handleSyncError(TmdbWorkQueue queueItem, Exception e, Long personId) {
        queueItem.markAsFailed("Sync Error: " + e.getMessage());
        tmdbWorkQueueRepository.save(queueItem);
        log.error("인물 {} 동기화 중 오류 발생", personId, e);
    }
}