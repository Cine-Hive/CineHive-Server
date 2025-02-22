package com.example.CineHive.service.creditService.drama;

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
    public void saveDramaCredits(Long dramaId) {
        String response = webClient.get()
                .uri("/tv/" + dramaId + "/credits?api_key=" + apiKey + "&language=ko")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode castNode = rootNode.path("cast");

                Drama drama = dramaRepository.findById(dramaId).orElse(null);
                if (drama != null) {
                    int mainActorCount = 3; // 필요한 주연 배우 수 설정
                    for (int i = 0; i < Math.min(castNode.size(), mainActorCount); i++) {
                        JsonNode castMember = castNode.get(i);
                        Actor actor = new Actor();
                        actor.setName(castMember.get("name").asText());

                        // 중복 확인
                        boolean alreadyExists = drama.getActors().stream()
                                .anyMatch(existingActor -> existingActor.getName().equals(actor.getName()));

                        if (!alreadyExists) {
                            // Drama에 Actor 추가
                            drama.getActors().add(actor);
                            actor.setDrama(drama); // Actor에 Drama 설정
                        }
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
    }
}
