package com.example.CineHive.controller.movie;

import com.example.CineHive.dto.video.common.VideoDto;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.service.credit.movie.SimilarMovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Movie Controller", description = "영화 정보 관련 기능을 제공하는 API")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SimilarMovieService similarMovieService;

    @Operation(summary = "DB에서 영화 받아오기", description = "movie 테이블에 저장된 모든 movie 정보를 리스트 형태로 반환")
    @GetMapping("/movies")
    @ResponseBody
    public List<VideoDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToVideoDto)
                .collect(Collectors.toList());
    }

    @Operation(summary = "영화 상세 페이지 조회", description = "해당 Moive ID로 영화 상세 정보를 상세 페이지에 반환, 존재하지 않는 경우 404 응답을 반환")
    @GetMapping("/movies/{id}")
    @ResponseBody
    public ResponseEntity<VideoDto> getMovieById(@PathVariable Long id) {
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isPresent()) {
            return ResponseEntity.ok(convertToVideoDto(movieOptional.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "관련 추천 영화 조회", description = "특정 영화 ID로 TMDB의 추천 영화 목록을 가져옴")
    @GetMapping("/movies/{id}/similar")
    @ResponseBody
    public ResponseEntity<List<VideoDto>> getSimilarMovies(@PathVariable Long id) {
        List<Movie> similarMovies = similarMovieService.getSimilarMovies(id);
        List<VideoDto> videoDtos = similarMovies.stream()
                .map(this::convertToVideoDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(videoDtos);
    }
    
    // Movie 객체를 VideoDto로 변환하는 헬퍼 메서드
    private VideoDto convertToVideoDto(Movie movie) {
        List<String> genreNames = movie.getGenres().stream()
                .map(genre -> genre.getName())
                .collect(Collectors.toList());
                
        return new VideoDto(
                movie.getId(),
                movie.getPosterPath(),
                movie.getTitle(),
                movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null,
                genreNames
        );
    }
}
