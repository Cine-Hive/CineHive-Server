package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.datasync.domain.repository.*;
import com.example.CineHive.datasync.dto.MovieDelta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieSyncService {

    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieKeywordRepository movieKeywordRepository;
    private final MovieCastRepository movieCastRepository;
    private final MovieCrewRepository movieCrewRepository;

    @Transactional
    public void syncMovie(MovieDelta delta) {
        // 1. Movie 본체 엔티티 저장 (ID가 PK이므로 JpaRepository.save가 UPSERT 역할을 함)
        movieRepository.save(delta.movie());

        // 2. 관계 데이터는 기존 것을 모두 지우고 새로 삽입 (Full Sync에서 가장 간단하고 멱등성을 보장하는 방법)
        Long movieId = delta.movie().getTmdbId();

        if (delta.genres() != null && !delta.genres().isEmpty()) {
            movieGenreRepository.deleteAllByMovieId(movieId);
            movieGenreRepository.saveAll(delta.genres());
        }

        if (delta.keywords() != null && !delta.keywords().isEmpty()) {
            movieKeywordRepository.deleteAllByMovieId(movieId);
            movieKeywordRepository.saveAll(delta.keywords());
        }

        if (delta.cast() != null && !delta.cast().isEmpty()) {
            movieCastRepository.deleteAllByMovieId(movieId);
            movieCastRepository.saveAll(delta.cast());
        }

        if (delta.crew() != null && !delta.crew().isEmpty()) {
            movieCrewRepository.deleteAllByMovieId(movieId);
            movieCrewRepository.saveAll(delta.crew());
        }
    }
}