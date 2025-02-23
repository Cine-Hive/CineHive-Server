package com.example.CineHive.service.creditService.drama;

import com.example.CineHive.dto.video.drama.DirectorDto;
import com.example.CineHive.entity.credit.drama.Director;
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
public class DramaDirectorService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private DramaRepository dramaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public DramaDirectorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    @Transactional
    public List<DirectorDto> saveDramaDirectors(Long dramaId) {
        String response = webClient.get()
                .uri("/tv/" + dramaId + "/credits?api_key=" + apiKey + "&language=en-US")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<DirectorDto> directorDTOs = new ArrayList<>();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode crewNode = rootNode.path("crew");

                Drama drama = dramaRepository.findById(dramaId).orElse(null);
                if (drama != null) {
                    // directors 리스트가 null일 경우 초기화
                    if (drama.getDirectors() == null) {
                        drama.setDirectors(new ArrayList<>()); // 빈 리스트로 초기화
                    }

                    for (JsonNode crewMember : crewNode) {
                        // 감독 정보를 찾기 위해 "job" 속성이 "Director"인 경우만 필터링
                        if ("Director".equals(crewMember.get("job").asText())) {
                            Director director = new Director();
                            director.setName(crewMember.get("name").asText());


                            drama.getDirectors().add(director);
                            directorDTOs.add(new DirectorDto(director.getId(), director.getName()));

                            break;
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
        return directorDTOs;
    }
}
