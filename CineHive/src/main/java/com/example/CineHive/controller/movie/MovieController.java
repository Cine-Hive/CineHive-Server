package com.example.CineHive.controller.movie;

import com.example.CineHive.dto.video.movie.NowPlayingMovieDto;
import com.example.CineHive.dto.video.movie.TopRatedMovieDto;
import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.entity.videotype.Drama;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.service.credit.animation.AnimationService;
import com.example.CineHive.service.credit.drama.DramaService;
import com.example.CineHive.service.credit.movie.MovieService;
import com.example.CineHive.service.credit.movie.NowPlayingMovieService;
import com.example.CineHive.service.credit.movie.TopRatedMovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Tag(name = "Movie Controller", description = "영화 정보 관련 기능을 제공하는 API")

public class MovieController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private DramaService dramaService;
    @Autowired
    private AnimationService animationService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private TopRatedMovieService topRatedMovieService;

    @Operation(summary = "DB에서 영화 받아오기", description = "movie 테이블에 저장된 모든 movie 정보를 리스트 형태로 반환")
    @GetMapping("/movies")
    @ResponseBody
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Operation(summary = "클라이언트 검색", description = "검색어를 받아 해당 검색어를 포함하는 Moive(animation 제외), 드라마(애니메이션 제외), 애니메이션을 반환")
    @PostMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestBody Map<String, String> request) {
        String query = request.get("query");

        System.out.println("Request received for searching movies: " + query);
        List<Movie> searchResults1 = movieService.searchMovies(query);  // 검색 결과 받기
        List<Drama> searchResults2 = dramaService.searchDramas(query);  // 검색 결과 받기
        List<Animation> searchResults3 = animationService.searchAnimations(query);  // 검색 결과 받기

        Map<String,Object> response = new HashMap<>();
        response.put("movies", searchResults1);
        response.put("dramas", searchResults2);
        response.put("animations", searchResults3);

        return ResponseEntity.ok().body(response);  // 검색 결과를 클라이언트로 반환
    }


    @Operation(summary = "영화 상세 페이지 조회", description = "해당 Moive ID로 영화 상세 정보를 상세 페이지에 반환, 존재하지 않는 경우 404 응답을 반환")
    @GetMapping("/movies/{id}")
    @ResponseBody
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isPresent()) {
            return ResponseEntity.ok(movieOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
