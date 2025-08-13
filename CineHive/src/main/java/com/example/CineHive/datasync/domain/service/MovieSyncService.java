package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.datasync.dto.MovieDelta;
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
public class MovieSyncService {

    // 영화와 관련된 모든 리포지토리 주입
    private final MovieRepository movieRepository;
    private final CollectionRepository collectionRepository;
    private final ProductionCompanyRepository productionCompanyRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieKeywordRepository movieKeywordRepository;
    private final MovieCastRepository movieCastRepository;
    private final MovieCrewRepository movieCrewRepository;
    private final MovieProductionCompanyRepository movieProductionCompanyRepository;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;
    
    // TMDB API 클라이언트
    private final TmdbApiClient tmdbApiClient;

    /**
     * 배치 처리용 영화 동기화 메소드
     * TMDB API 호출 + 큐 관리를 포함한 완전한 동기화
     */
    @Transactional
    public void syncMovieFromQueue(TmdbWorkQueue queueItem) {
        Long movieId = queueItem.getTmdbId();
        
        try {
            // 1. TMDB API 호출로 상세 정보 가져오기
            TmdbMovieDetailResponse tmdbResponse = tmdbApiClient.getMovieDetailForBatch(movieId);
            
            // 2. TMDB 응답을 MovieDelta로 변환
            MovieDelta delta = MovieDelta.fromTmdbResponse(tmdbResponse);
            
            // 3. 데이터베이스에 동기화
            syncMovie(delta);
            
            // 4. 큐 아이템을 처리 완료로 마킹
            queueItem.markAsProcessed();
            tmdbWorkQueueRepository.save(queueItem);
            
            log.debug("영화 동기화 완료: movieId={}", movieId);
            
        } catch (TmdbClientException e) {
            handleTmdbApiError(queueItem, e, movieId);
        } catch (Exception e) {
            handleSyncError(queueItem, e, movieId);
        }
    }
    
    /**
     * MovieDelta를 받아서 데이터베이스에 동기화하는 메소드
     * 기존 로직 유지 (delete-insert 패턴)
     */
    @Transactional
    public void syncMovie(MovieDelta delta) {
        Long movieId = delta.movie().getTmdbId();

        // 1. 독립적인 엔티티들 먼저 저장 (UPSERT)
        // Collection 정보가 있으면 저장
        if (delta.collection() != null) {
            collectionRepository.save(delta.collection());
        }
        // ProductionCompany 정보가 있으면 모두 저장
        if (delta.companies() != null && !delta.companies().isEmpty()) {
            productionCompanyRepository.saveAll(delta.companies());
        }

        // 2. Movie 본체 엔티티 저장 (UPSERT)
        movieRepository.save(delta.movie());

        // 3. 관계 데이터는 기존 것을 모두 지우고 새로 삽입 (Full Sync에서의 멱등성 보장)
        movieGenreRepository.deleteAllByMovieId(movieId);
        if (delta.genres() != null && !delta.genres().isEmpty()) {
            movieGenreRepository.saveAll(delta.genres());
        }

        movieKeywordRepository.deleteAllByMovieId(movieId);
        if (delta.keywords() != null && !delta.keywords().isEmpty()) {
            movieKeywordRepository.saveAll(delta.keywords());
        }

        movieCastRepository.deleteAllByMovieId(movieId);
        if (delta.cast() != null && !delta.cast().isEmpty()) {
            movieCastRepository.saveAll(delta.cast());
        }

        movieCrewRepository.deleteAllByMovieId(movieId);
        if (delta.crew() != null && !delta.crew().isEmpty()) {
            movieCrewRepository.saveAll(delta.crew());
        }

        movieProductionCompanyRepository.deleteAllByMovieId(movieId);
        if (delta.movieCompanies() != null && !delta.movieCompanies().isEmpty()) {
            movieProductionCompanyRepository.saveAll(delta.movieCompanies());
        }

        // 4. Outbox 패턴을 이용한 캐시 무효화 이벤트 기록 (추후 구현)
    }
    
    private void handleTmdbApiError(TmdbWorkQueue queueItem, TmdbClientException e, Long movieId) {
        if (e.getStatus() == HttpStatus.NOT_FOUND) {
            // 404는 영구 스킵 처리
            queueItem.markAsProcessed();
            queueItem.setLastError("Movie not found in TMDB: " + movieId);
            log.warn("영화 ID {}를 TMDB에서 찾을 수 없어 스킵합니다.", movieId);
        } else {
            // 기타 API 에러는 재시도 대상
            queueItem.markAsFailed("TMDB API Error: " + e.getMessage());
            log.error("영화 {} TMDB API 호출 실패: {}", movieId, e.getMessage());
        }
        tmdbWorkQueueRepository.save(queueItem);
    }
    
    private void handleSyncError(TmdbWorkQueue queueItem, Exception e, Long movieId) {
        queueItem.markAsFailed("Sync Error: " + e.getMessage());
        tmdbWorkQueueRepository.save(queueItem);
        log.error("영화 {} 동기화 중 오류 발생", movieId, e);
    }
}