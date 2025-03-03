package com.example.CineHive.service.creditService.animation;

import com.example.CineHive.dto.video.animation.VideoDto;
import com.example.CineHive.entity.credit.animation.Video;
import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.entity.credit.animation.Genre;
import com.example.CineHive.repository.videos.animation.AnimationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnimationService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    @Autowired
    private AnimationRepository animationRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private AnimationDirectorService animationDirectorService;

    @Autowired
    private AnimationVideoService animationVideoService;

    @Autowired
    private AnimationGenreService animationGenreService;

    public AnimationService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());  // 추가: LocalDate 변환 지원
    }

    public List<Animation> searchAnimations(String query) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/search/movie?query=" // TV에서 영화로 변경
                        + UriUtils.encode(query, StandardCharsets.UTF_8)
                        + "&api_key=" + apiKey
                        + "&include_adult=true&language=ko&page=1")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<Animation> animations = new ArrayList<>();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode animationsNode = rootNode.path("results");

                for (JsonNode animationNode : animationsNode) {
                    List<Integer> genreIds = objectMapper.convertValue(animationNode.get("genre_ids"), List.class);
                    // 애니메이션 장르(16)만 필터링
                    if (!genreIds.contains(16)) {
                        continue;
                    }

                    Long animationId = animationNode.get("id").asLong();
                    String posterPath = animationNode.get("poster_path").asText();
                    String backDropPath = animationNode.get("backdrop_path").asText();

                    // 포스터 없는 경우 제외
                    if (posterPath == null || posterPath.isEmpty()) {
                        continue;
                    }

                    Animation animation = new Animation();
                    animation.setId(animationId);
                    animation.setName(animationNode.get("title").asText());
                    animation.setOverview(animationNode.get("overview").asText());
                    animation.setPosterPath(posterPath);
                    animation.setBackDropPath(backDropPath);
                    animation.setVoteAverage(animationNode.get("vote_average").asDouble());
                    animation.setPopularity(animationNode.get("popularity").asDouble());
                    String releaseDateString = animationNode.get("release_date").asText();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                    animation.setReleaseDate(releaseDate);


                    List<Genre> genres = new ArrayList<>();
                    for (JsonNode genreNode : animationNode.get("genre_ids")) {
                        Genre genre = new Genre();
                        genre.setId(genreNode.asInt());
                        genre.setName(animationGenreService.getGenreNameById(genre.getId()));

                        genre.setAnimation(animation);

                        genres.add(genre);
                    }
                    animation.setGenres(genres);


                    // 비디오 정보를 가져옴
                    VideoDto videoDto = animationVideoService.getFirstVideoForAnimation(animationId);
                    if (videoDto != null) {
                        Video video = new Video();
                        video.setVideoKey(videoDto.getVideoKey());
                        video.setName(videoDto.getName());
                        video.setAnimation(animation);
                        animation.getVideos().add(video);
                        System.out.println("Added video: " + video.getName());
                    }

                    animationRepository.save(animation);
                    System.out.println("Saved animation: " + animation.getName());

                    animationDirectorService.saveAnimationDirectors(animationId);

                    animations.add(animation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return animations;
    }
}
