package com.example.CineHive.controller;

import com.example.CineHive.dto.Content;
import com.example.CineHive.dto.PreferredGenereDto;
import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.entity.videotype.Drama;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.videos.animation.AnimationRepository;
import com.example.CineHive.repository.videos.drama.DramaRepository;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Preferred Genre Controller", description = "선호하는 장르 정보 관련 기능을 제공하는 API")
@RestController
public class PreferredGenreController {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private DramaRepository dramaRepository;
    @Autowired
    private AnimationRepository animationRepository;

    @Operation(summary = "선호 장르 선택",description = "사용자가 드라마,영화,애니메이션 중 선택한 장르에 맞는 영상 반환")
    @PostMapping("/preferredGenres")
    public ResponseEntity<List<Content>> getContentByGenres(@RequestBody PreferredGenereDto preferredGenereDto) {

        System.out.println("전송된 선호 장르: " + preferredGenereDto.getGenres());

        List<Content> contents = new ArrayList<>();

        for (String genre : preferredGenereDto.getGenres()) {
            switch (genre) {
                case "드라마":
                    List<Drama> dramas = dramaRepository.findAll();
                    for (Drama drama : dramas) {
                        contents.add(convertToContent(drama));
                        if (contents.size() >= 18) break;
                    }
                    break;
                case "애니메이션":
                    List<Animation> animations = animationRepository.findAll();
                    for (Animation animation : animations) {
                        contents.add(convertToContent(animation));
                        if (contents.size() >= 18) break;
                    }
                    break;
                case "영화":
                    List<Movie> movies = movieRepository.findAll();
                    for (Movie movie : movies) {
                        contents.add(convertToContent(movie));
                        if (contents.size() >= 18) break;
                    }
                    break;
            }
            if (contents.size() >= 18) break; // 모든 장르에서 18개 이상이면 종료
        }

        if (contents.isEmpty()) {
            System.out.println("선호 장르에 대한 콘텐츠가 없습니다.");
        }

        return ResponseEntity.ok(contents);
    }

    private Content convertToContent(Drama drama) {
        return new Content(drama.getId(), drama.getName(), drama.getOverview(), drama.getPosterPath(), "드라마");
    }

    private Content convertToContent(Animation animation) {
        return new Content(animation.getId(), animation.getName(), animation.getOverview(), animation.getPosterPath(), "애니메이션");
    }

    private Content convertToContent(Movie movie) {
        return new Content(movie.getId(), movie.getTitle(), movie.getOverview(), movie.getPosterPath(), "영화");
    }
}
