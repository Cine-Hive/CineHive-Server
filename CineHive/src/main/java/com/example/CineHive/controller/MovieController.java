package com.example.CineHive.controller;

import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.entity.videotype.Drama;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.TopMovie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.TopMovieRepository;
import com.example.CineHive.service.creditService.animation.AnimationService;
import com.example.CineHive.service.creditService.drama.DramaService;
import com.example.CineHive.service.creditService.movie.MovieService;
import com.example.CineHive.service.creditService.movie.NowPlayingMovieService;
import com.example.CineHive.service.creditService.movie.TopRatedMovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
    private NowPlayingMovieService nowPlayingMovieService;

    @Autowired
    //현재 상영중인 영화 DB에 넣기 (수동으로 저장)
    private TopRatedMovieService topRatedMovieService;
    @Operation(summary = "현재 상영중인 영화 수동으로 DB에 저장", description = "현재 상영중인 영화 목록을 movie 테이블에 저장")
    @PostMapping("/update_now_playing")
    public ResponseEntity<?> getNowPlayingMovies() {
        System.out.println("Request received for now playing movies");
        movieService.saveMoviesToDatabase();  // 매개변수로 language와 page 전달
        return ResponseEntity.ok().body("성공적으로 데이터를 저장했습니다!");
    }

    //데이터베이스에 있는 영화를 그냥 다 가져오는 것(homepage.vue 열면 바로 실행)
    @Operation(summary = "DB에서 영화 받아오기", description = "movie 테이블에 저장된 모든 movie 정보를 리스트 형태로 반환")
    @GetMapping("/movies")
    @ResponseBody
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    //Topmovie 데이블에서 가져오기
    @Operation(summary = "평점순 영화 받아오기", description = "topmovie 테이블에 저장된 topmovie 정보를 리스트 형태로 반환")
    @GetMapping("/get_topmovies")
    @ResponseBody
    public ResponseEntity<List<TopMovie>> getTopRatedMoviesList() {
        Pageable pageable = PageRequest.of(0, 22); // 첫 번째 페이지에서 22개 가져오기
        List<TopMovie> topRatedMovies = topRatedMovieService.getTopRatedMovies(pageable);
        return ResponseEntity.ok(topRatedMovies);
    }

    //TopRated 영화 DB에 넣기 (수동으로 저장)
    @Operation(summary = "Top rated 영화 수동으로 DB에 저장", description = "api로 받아온 topmovie 목록을 topmovie 테이블에 저장")
    @PostMapping("/update_top_movie")
    public ResponseEntity<?> getTopMovies() {
        System.out.println("Request received for Top movies");
        movieService.saveTopRatedMoviesToDatabase();
        return ResponseEntity.ok().body("성공적으로 데이터를 저장했습니다!");
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


    @Operation(summary = "Moive 상세 페이지 조회", description = "해당 Moive ID로 영화 상세 정보를 상세 페이지에 반환, 존재하지 않는 경우 404 응답을 반환")
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

    @Operation(summary = "DB에 저장된 영화 출력", description = "movie 테이블에 저장된 movie 정보를 리스트 형태로 반환")
    @GetMapping("/now_playing")
    @ResponseBody
    public ResponseEntity<List<Movie>> getNowPlayingMoviesList() {
        Pageable pageable = PageRequest.of(0, 22); // 첫 번째 페이지에서 22개 가져오기
        List<Movie> nowPlayingMovies = nowPlayingMovieService.getNowPlayingMovies(pageable);
        return ResponseEntity.ok(nowPlayingMovies);
    }
}
