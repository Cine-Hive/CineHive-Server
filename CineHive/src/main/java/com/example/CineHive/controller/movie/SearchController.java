package com.example.CineHive.controller.movie;

import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.entity.videotype.Drama;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.service.credit.animation.AnimationService;
import com.example.CineHive.service.credit.drama.DramaService;
import com.example.CineHive.service.credit.movie.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Search Controller", description = "드라마 & 영화 & 애니메이션 검색 기능을 제공하는 API")
@RestController
public class SearchController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private DramaService dramaService;
    @Autowired
    private AnimationService animationService;

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

        return ResponseEntity.ok().body(response);
    }
}
