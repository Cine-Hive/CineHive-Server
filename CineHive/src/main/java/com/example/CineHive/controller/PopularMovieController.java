package com.example.CineHive.controller;

import com.example.CineHive.dto.video.movie.PopularMovieDto;
import com.example.CineHive.dto.video.movie.UpComingMovieDto;
import com.example.CineHive.service.creditService.movie.PopularMovieService;
import com.example.CineHive.service.creditService.movie.UpComingMovieService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PopularMovieController {

        @Autowired
        private PopularMovieService popularMovieService;


        @Operation(summary = "인기 영화 수동으로 DB에 저장", description = "API로 받아온 인기 영화 목록을 upcoming 테이블에 저장")
        @PostMapping("/update_popluar")
        public ResponseEntity<?> getPopularMovies() {
            System.out.println("Request received for popular movies");
            popularMovieService.savePopularMoviesToDatabase();
            return ResponseEntity.ok().body("성공적으로 데이터를 저장했습니다!");
        }


        @Operation(summary = "인기 영화 조회", description = "popular 테이블에 저장된 인기 영화 정보를 리스트 형태로 반환")
        @GetMapping("/get_popular_movies")
        @ResponseBody
        public ResponseEntity<List<PopularMovieDto>> getTopRatedMoviesList() {
            Pageable pageable = PageRequest.of(0, 22); // 첫 번째 페이지에서 22개 가져오기
            List<PopularMovieDto> popularMovies = popularMovieService.getPopularMovies(pageable);
            return ResponseEntity.ok(popularMovies);
        }
    }
