package com.example.CineHive.Async;

import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.service.credit.movie.MovieActorService;
import com.example.CineHive.service.credit.movie.MovieDirectorService;
import com.example.CineHive.service.credit.movie.SimilarMovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class AsyncHelper {

    private final MovieRepository movieRepository;
    private final SimilarMovieService similarMovieService;
    private final MovieActorService movieActorService;
    private final MovieDirectorService movieDirectorService;

    @Async
    public void saveAdditionalMovieData(Long movieId) {
        try {
            List<Movie> similarMovies = similarMovieService.getSimilarMovies(movieId);
            for (Movie similar : similarMovies) {
                if (!movieRepository.existsById(similar.getId())) {
                    movieRepository.save(similar);
                    movieActorService.saveMovieCredits(similar.getId());
                    movieDirectorService.saveMovieDirectors(similar.getId());
                }
            }
        } catch (Exception e) {
            System.out.println("비동기 저장 오류: " + e.getMessage());
        }
    }
}