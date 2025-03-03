package com.example.CineHive.service.creditService.drama;

import com.example.CineHive.dto.video.drama.ActorDto;
import com.example.CineHive.entity.credit.drama.Actor;
import com.example.CineHive.entity.videotype.Drama;
import com.example.CineHive.repository.videos.drama.DramaRepository;
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
public class DramaActorService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private DramaRepository dramaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public DramaActorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    @Transactional
    public List<ActorDto> saveDramaCredits(Long dramaId) {
        String response = webClient.get()
                .uri("/tv/" + dramaId + "/credits?api_key=" + apiKey + "&language=ko")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<ActorDto> actorDTOs = new ArrayList<>();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode castNode = rootNode.path("cast");

                Drama drama = dramaRepository.findById(dramaId).orElse(null);
                if (drama != null) {
                    for (JsonNode castMember : castNode) {
                        Actor actor = new Actor();
                        //배우 이름
                        actor.setName(castMember.get("name").asText());

                        // 배우의 이미지 URL
                        String profilePath = castMember.path("profile_path").asText();
                        if (!profilePath.isEmpty()) {
                            String posterUrl = "https://image.tmdb.org/t/p/w500" + profilePath;
                            actor.setPosterPath(posterUrl);
                        }

                        boolean alreadyExists = drama.getActors().stream()
                                .anyMatch(existingActor -> existingActor.getName().equals(actor.getName()));

                        if (!alreadyExists) {
                            drama.getActors().add(actor);
                            actor.setDrama(drama);
                        }

                        ActorDto actorDto = new ActorDto();
                        actorDto.setId(actor.getId());
                        actorDto.setName(actor.getName());
                        actorDto.setPosterPath(actor.getPosterPath());
                        actorDTOs.add(actorDto);
                    }
                    dramaRepository.save(drama);
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
