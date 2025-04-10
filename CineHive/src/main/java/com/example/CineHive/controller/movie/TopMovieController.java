package com.example.CineHive.controller.movie;

import com.example.CineHive.dto.video.common.VideoDto;
import com.example.CineHive.service.credit.movie.TopRatedMovieService;
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

@Tag(name = "TopMovie Controller", description = "평점 순위 영화 기능을 제공하는 API")
@RestController
public class TopMovieController {

    @Autowired
    private TopRatedMovieService topRatedMovieService;

    //Topmovie 데이블에서 가져오기
    @Operation(summary = "평점순 영화 조회", description = "topmovie 테이블에 저장된 topmovie 정보를 리스트 형태로 반환")
    @GetMapping("/movies/top-rated")
    @ResponseBody
    public ResponseEntity<List<VideoDto>> getTopRatedMoviesList() {
        Pageable pageable = PageRequest.of(0, 22); // 첫 번째 페이지에서 22개 가져오기
        List<VideoDto> topRatedMovies = topRatedMovieService.getTopRatedMovies(pageable);
        return ResponseEntity.ok(topRatedMovies);
    }

    //TopRated 영화 DB에 넣기 (수동으로 저장)
    @Operation(summary = "평점 순위 영화 수동으로 DB에 저장", description = "api로 받아온 평점 순위 영화 목록을 topmovie 테이블에 저장")
    @PostMapping("/movies/top-rated")
    public ResponseEntity<?> saveTopRatedMovies() {
        System.out.println("Request received for Top movies");
        topRatedMovieService.saveTopRatedMoviesToDatabase();
        return ResponseEntity.ok().body("성공적으로 데이터를 저장했습니다!");
    }
}
