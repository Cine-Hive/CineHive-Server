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
    private final PersonRepository personRepository;  // Person 리포지토리 추가
    private final GenreRepository genreRepository;
    private final KeywordRepository keywordRepository;
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
     * 저장 순서: 참조 데이터 -> Movie 본체 -> 관계 테이블 (FK 제약조건 해결)
     */
    @Transactional
    public void syncMovie(MovieDelta delta) {
        Long movieId = delta.movie().getTmdbId();
        log.debug("영화 동기화 시작: movieId={}", movieId);

        try {
            // ========== 1단계: 참조/기준 데이터 저장 (UPSERT) ==========
            // 이 데이터들은 관계 테이블이 참조하므로 반드시 먼저 저장되어야 함
            
            // 1-1. Genre 저장 (movie_genre가 참조)
            if (delta.genreEntities() != null && !delta.genreEntities().isEmpty()) {
                log.debug("Saving {} genres for movie {}", delta.genreEntities().size(), movieId);
                genreRepository.saveAll(delta.genreEntities());
                genreRepository.flush();
            }
            
            // 1-2. Keyword 저장 (movie_keyword가 참조)
            if (delta.keywordEntities() != null && !delta.keywordEntities().isEmpty()) {
                log.debug("Saving {} keywords for movie {}", delta.keywordEntities().size(), movieId);
                keywordRepository.saveAll(delta.keywordEntities());
                keywordRepository.flush();
            }
            
            // 1-3. Person 저장 (movie_cast/movie_crew가 참조)
            if (delta.persons() != null && !delta.persons().isEmpty()) {
                log.debug("Saving {} persons for movie {}", delta.persons().size(), movieId);
                personRepository.saveAll(delta.persons());
                personRepository.flush();
            }
            
            // 1-4. ProductionCompany 저장 (movie_production_company가 참조)
            if (delta.companies() != null && !delta.companies().isEmpty()) {
                log.debug("Saving {} production companies for movie {}", delta.companies().size(), movieId);
                productionCompanyRepository.saveAll(delta.companies());
                productionCompanyRepository.flush();
            }
            
            // 1-5. Collection 저장 (movie가 참조)
            if (delta.collection() != null) {
                log.debug("Saving collection for movie {}", movieId);
                collectionRepository.save(delta.collection());
                collectionRepository.flush();
            }

            // ========== 2단계: Movie 본체 저장 (UPSERT) ==========
            log.debug("Saving movie entity: movieId={}", movieId);
            movieRepository.save(delta.movie());
            movieRepository.flush();

            // ========== 3단계: 관계 테이블 저장 (Delete-Insert) ==========
            // 양쪽 FK가 모두 존재함이 보장된 후 실행
            
            // 3-1. movie_genre
            movieGenreRepository.deleteAllByMovieId(movieId);
            if (delta.genres() != null && !delta.genres().isEmpty()) {
                try {
                    movieGenreRepository.saveAll(delta.genres());
                    log.debug("Saved {} movie_genre relations for movie {}", delta.genres().size(), movieId);
                } catch (Exception e) {
                    log.error("FK 위반: movie_genre 저장 실패 - movieId={}, genreIds={}", 
                        movieId, delta.genres().stream().map(mg -> mg.getGenreId()).toList(), e);
                    throw e;
                }
            }

            // 3-2. movie_keyword
            movieKeywordRepository.deleteAllByMovieId(movieId);
            if (delta.keywords() != null && !delta.keywords().isEmpty()) {
                try {
                    movieKeywordRepository.saveAll(delta.keywords());
                    log.debug("Saved {} movie_keyword relations for movie {}", delta.keywords().size(), movieId);
                } catch (Exception e) {
                    log.error("FK 위반: movie_keyword 저장 실패 - movieId={}, keywordIds={}", 
                        movieId, delta.keywords().stream().map(mk -> mk.getKeywordId()).toList(), e);
                    throw e;
                }
            }

            // 3-3. movie_cast
            movieCastRepository.deleteAllByMovieId(movieId);
            if (delta.cast() != null && !delta.cast().isEmpty()) {
                try {
                    movieCastRepository.saveAll(delta.cast());
                    log.debug("Saved {} movie_cast relations for movie {}", delta.cast().size(), movieId);
                } catch (Exception e) {
                    log.error("FK 위반: movie_cast 저장 실패 - movieId={}, personIds={}", 
                        movieId, delta.cast().stream().map(mc -> mc.getPersonId()).toList(), e);
                    throw e;
                }
            }

            // 3-4. movie_crew
            movieCrewRepository.deleteAllByMovieId(movieId);
            if (delta.crew() != null && !delta.crew().isEmpty()) {
                try {
                    movieCrewRepository.saveAll(delta.crew());
                    log.debug("Saved {} movie_crew relations for movie {}", delta.crew().size(), movieId);
                } catch (Exception e) {
                    log.error("FK 위반: movie_crew 저장 실패 - movieId={}, personIds={}", 
                        movieId, delta.crew().stream().map(mc -> mc.getPersonId()).toList(), e);
                    throw e;
                }
            }

            // 3-5. movie_production_company
            movieProductionCompanyRepository.deleteAllByMovieId(movieId);
            if (delta.movieCompanies() != null && !delta.movieCompanies().isEmpty()) {
                try {
                    movieProductionCompanyRepository.saveAll(delta.movieCompanies());
                    log.debug("Saved {} movie_production_company relations for movie {}", delta.movieCompanies().size(), movieId);
                } catch (Exception e) {
                    log.error("FK 위반: movie_production_company 저장 실패 - movieId={}, companyIds={}", 
                        movieId, delta.movieCompanies().stream().map(mpc -> mpc.getCompanyId()).toList(), e);
                    throw e;
                }
            }

            log.info("영화 동기화 완료: movieId={}", movieId);
            
        } catch (Exception e) {
            log.error("영화 동기화 실패: movieId={}, error={}", movieId, e.getMessage(), e);
            throw e;
        }
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