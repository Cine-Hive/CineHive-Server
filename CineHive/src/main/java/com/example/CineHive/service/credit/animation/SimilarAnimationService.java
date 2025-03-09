package com.example.CineHive.service.credit.animation;

import com.example.CineHive.dto.video.animation.VideoDto;
import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.entity.videotype.RecommendationAnimation;
import com.example.CineHive.repository.videos.animation.AnimationRecommendationRepository;
import com.example.CineHive.repository.videos.animation.AnimationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SimilarAnimationService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Autowired
    private AnimationRepository animationRepository;

    @Autowired
    private AnimationRecommendationRepository animationRecommendationRepository;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    @Autowired
    private AnimationVideoService animationVideoService;
    @Autowired
    private AnimationDirectorService animationDirectorService;

    public SimilarAnimationService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    public void saveRecommendedAnimations(Long animationId, List<Animation> similarAnimations) {
        Animation animation = animationRepository.findById(animationId)
                .orElseThrow(() -> new RuntimeException("Animation not found"));

        for (Animation similarAnimation : similarAnimations) {
            Optional<Animation> existingRecommendedAnimation = animationRepository.findById(similarAnimation.getId());
            if (existingRecommendedAnimation.isPresent()) {
                RecommendationAnimation recommendationAnimation = new RecommendationAnimation();
                recommendationAnimation.setAnimation(animation);
                recommendationAnimation.setRecommendedAnimation(existingRecommendedAnimation.get());
                animationRecommendationRepository.save(recommendationAnimation);
            }
        }
    }

    public List<Animation> getSimilarAnimations(Long animationId) {
        String url = "https://api.themoviedb.org/3/movie/" + animationId + "/similar?api_key=" + apiKey + "&language=ko&page=1";

        String response = webClient.get()
                .uri(url)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<Animation> similarAnimations = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode resultsNode = rootNode.path("results");

                for (int i = 0; i < Math.min(10, resultsNode.size()); i++) {
                    JsonNode animationNode = resultsNode.get(i);
                    Animation animation = new Animation();
                    animation.setId(animationNode.get("id").asLong());
                    animation.setName(animationNode.get("title").asText());
                    animation.setOverview(animationNode.get("overview").asText());
                    animation.setPosterPath(animationNode.get("poster_path").asText());
                    animation.setVoteAverage(animationNode.get("vote_average").asDouble());
                    animation.setPopularity(animationNode.get("popularity").asDouble());

                    String releaseDateString = animationNode.get("release_date").asText();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                    animation.setReleaseDate(releaseDate);
                    animation.setBackDropPath(animationNode.get("backdrop_path").asText());

                    String movieDetailsResponse = webClient.get()
                            .uri("https://api.themoviedb.org/3/movie/" + animation.getId() + "?api_key=" + apiKey + "&language=ko")
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                    JsonNode runtimeNode = movieDetailsNode.get("runtime");
                    animation.setRuntime(runtimeNode != null && !runtimeNode.isNull() ? runtimeNode.asInt() : 0);

                    animationDirectorService.saveAnimationDirectors(animationId);


                    // 비디오 정보 추가
                    VideoDto video = animationVideoService.getFirstVideoForAnimation(animation.getId());
                    animation.setVideos(video != null ? List.of() : new ArrayList<>());

                    similarAnimations.add(animation);
                }

                similarAnimations.sort(Comparator.comparingDouble(Animation::getPopularity).reversed()
                        .thenComparing(Comparator.comparingDouble(Animation::getVoteAverage).reversed())
                        .thenComparing(Comparator.comparing(Animation::getReleaseDate).reversed()));

            } catch (Exception e) {
                e.printStackTrace();
            }

            saveRecommendedAnimations(animationId, similarAnimations);
        }

        return similarAnimations;
    }
}
