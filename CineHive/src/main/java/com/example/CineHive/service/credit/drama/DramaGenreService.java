package com.example.CineHive.service.credit.drama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class DramaGenreService {
    private final Map<Integer, String> genreMap = new HashMap<>();
    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public DramaGenreService(WebClient.Builder webClientBuilder, @Value("${tmdb.api.key}") String apiKey, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void loadGenres() {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/genre/tv/list?api_key=" + apiKey + "&language=ko")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode genresNode = rootNode.path("genres");

                for (JsonNode genreNode : genresNode) {
                    Integer id = genreNode.get("id").asInt();
                    String name = genreNode.get("name").asText();
                    genreMap.put(id, name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("장르 정보를 가져오는 데 실패했습니다.");
        }
    }

    public String getGenreNameById(Integer genreId) {
        return genreMap.getOrDefault(genreId, "기타");
    }
}
