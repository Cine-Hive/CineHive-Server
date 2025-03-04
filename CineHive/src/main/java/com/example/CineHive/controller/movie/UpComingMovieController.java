package com.example.CineHive.controller.movie;

import com.example.CineHive.dto.video.movie.UpComingMovieDto;
import com.example.CineHive.service.credit.movie.UpComingMovieService;
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
@Tag(name = "UpComing Movie Controller", description = "개봉 예정인 영화 기능을 제공하는 API")
@RestController
public class UpComingMovieController {

    @Autowired
    private UpComingMovieService upComingMovieService;


    @Operation(summary = "개봉 예정중인 영화 수동으로 DB에 저장", description = "API로 받아온 개봉 예정인 영화 목록을 upcoming 테이블에 저장")
    @PostMapping("/update_upcoming_movie")
    public ResponseEntity<?> getUpComingMovies() {
        System.out.println("Request received for upComing movies");
        upComingMovieService.saveUpComingMoviesToDatabase();
        return ResponseEntity.ok().body("성공적으로 데이터를 저장했습니다!");
    }


    @Operation(summary = "개봉 예정중인 영화 조회", description = "upcoming 테이블에 저장된 개봉 예정 영화 정보를 리스트 형태로 반환")
    @GetMapping("/get_upcoming_movies")
    @ResponseBody
    public ResponseEntity<List<UpComingMovieDto>> getTopRatedMoviesList() {
        Pageable pageable = PageRequest.of(0, 22); // 첫 번째 페이지에서 22개 가져오기
        List<UpComingMovieDto> upComingMovies = upComingMovieService.getUpComingMovies(pageable);
        return ResponseEntity.ok(upComingMovies);
    }

}
