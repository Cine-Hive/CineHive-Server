package com.example.CineHive.controller.movie;

import com.example.CineHive.dto.video.movie.NowPlayingMovieDto;
import com.example.CineHive.service.credit.movie.MovieService;
import com.example.CineHive.service.credit.movie.NowPlayingMovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "NowPlaying Controller", description = "현재 상영중인 영화 기능을 제공하는 API")
@RestController
public class NowPlayingMovieController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private NowPlayingMovieService nowPlayingMovieService;

    @Operation(summary = "현재 상영중인 영화 수동으로 DB에 저장", description = "현재 상영중인 영화 목록을 movie 테이블에 저장")
    @PostMapping("/update_now_playing")
    public ResponseEntity<?> getNowPlayingMovies() {
        System.out.println("Request received for now playing movies");
        movieService.saveMoviesToDatabase();  // 매개변수로 language와 page 전달
        return ResponseEntity.ok().body("성공적으로 데이터를 저장했습니다!");
    }

    @Operation(summary = "현재 상영중인 영화 조회", description = "movie 테이블에 저장된 movie 정보를 리스트 형태로 반환")
    @GetMapping("/now_playing")
    @ResponseBody
    public ResponseEntity<List<NowPlayingMovieDto>> getNowPlayingMoviesList() {
        Pageable pageable = PageRequest.of(0, 22); // 첫 번째 페이지에서 22개 가져오기
        List<NowPlayingMovieDto> nowPlayingMovies = nowPlayingMovieService.getNowPlayingMovies(pageable);
        return ResponseEntity.ok(nowPlayingMovies);
    }
}
