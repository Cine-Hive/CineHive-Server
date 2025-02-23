package com.example.CineHive.service.creditService.animation;

import com.example.CineHive.dto.video.animation.DirectorDto; // DTO 임포트
import com.example.CineHive.entity.credit.animation.Director; // 애니메이션 감독 엔티티
import com.example.CineHive.entity.videotype.Animation; // 애니메이션 엔티티
import com.example.CineHive.repository.videos.animation.AnimationRepository; // 애니메이션 리포지토리
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
public class AnimationDirectorService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private AnimationRepository animationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public AnimationDirectorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    @Transactional
    public List<DirectorDto> saveAnimationDirectors(Long animationId) {
        String response = webClient.get()
                .uri("/movie/" + animationId + "/credits?api_key=" + apiKey + "&language=en-US")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<DirectorDto> directorDTOs = new ArrayList<>();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode crewNode = rootNode.path("crew");

                Animation animation = animationRepository.findById(animationId).orElse(null);
                if (animation != null) {
                    for (JsonNode crewMember : crewNode) {
                        // 감독 정보를 찾기 위해 "job" 속성이 "Director"인 경우만 필터링
                        if ("Director".equals(crewMember.get("job").asText())) {
                            Director director = new Director();
                            director.setName(crewMember.get("name").asText());
                            director.setAnimation(animation);

                            animation.getDirectors().add(director);


                            DirectorDto directorDto = new DirectorDto();
                            directorDto.setId(director.getId());
                            directorDto.setName(director.getName());
                            directorDTOs.add(directorDto);

                            break;
                        }
                    }
                    animationRepository.save(animation);
                }
            } catch (Exception e) {
                System.out.println("JSON 처리 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return directorDTOs;
    }
}
