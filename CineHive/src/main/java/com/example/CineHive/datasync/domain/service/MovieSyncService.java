package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.datasync.dto.MovieDelta;
import com.example.CineHive.datasync.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}