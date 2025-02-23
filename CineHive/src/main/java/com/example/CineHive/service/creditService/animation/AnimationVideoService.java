package com.example.CineHive.service.creditService.animation;

import com.example.CineHive.entity.credit.animation.Video;
import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.repository.videos.animation.AnimationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AnimationVideoService {
    private final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Autowired
    private AnimationRepository animationRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AnimationVideoService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Video getFirstVideoForAnimation(Long animationId) {
        String url = BASE_URL + animationId + "/videos?api_key=" + apiKey;
        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return null;
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode resultsNode = rootNode.path("results");

            if (resultsNode.isArray() && resultsNode.size() > 0) {
                JsonNode firstVideoNode = resultsNode.get(0); // 첫 번째 비디오 가져오기

                Video video = new Video();
                video.setVideoKey(firstVideoNode.path("key").asText());
                video.setName(firstVideoNode.path("name").asText());

                // Animation과 관계 설정
                Animation animation = animationRepository.findById(animationId).orElse(null);
                if (animation != null) {
                    video.setAnimation(animation);
                    animation.getVideos().add(video);
                }

                return video;
            }
        } catch (Exception e) {
            System.out.println("Error fetching video: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
