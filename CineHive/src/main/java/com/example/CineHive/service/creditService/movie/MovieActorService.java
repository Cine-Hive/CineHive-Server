package com.example.CineHive.service.creditService.movie;

import com.example.CineHive.dto.video.movie.ActorDto;
import com.example.CineHive.entity.credit.movie.Actor;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieActorService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public MovieActorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    @Transactional
    public List<ActorDto> saveMovieCredits(Long movieId) {
        String response = webClient.get()
                .uri("/movie/" + movieId + "/credits?api_key=" + apiKey + "&language=ko")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<ActorDto> actorDTOs = new ArrayList<>();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode castNode = rootNode.path("cast");

                Movie movie = movieRepository.findById(movieId).orElse(null);
                if (movie != null) {
                    int mainActorCount = 10; // 필요한 주연 배우 수 설정
                    for (int i = 0; i < Math.min(castNode.size(), mainActorCount); i++) {
                        JsonNode castMember = castNode.get(i);
                        Actor actor = new Actor();
                        actor.setName(castMember.get("name").asText());

                        // 중복 확인
                        boolean alreadyExists = movie.getActors().stream()
                                .anyMatch(existingActor -> existingActor.getName().equals(actor.getName()));

                        if (!alreadyExists) {

                            movie.addActor(actor);
                            actorDTOs.add(new ActorDto(actor.getId(), actor.getName()));
                        }
                    }
                    movieRepository.save(movie);
                }
            } catch (Exception e) {
                System.out.println("JSON 처리 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return actorDTOs;
    }
}
