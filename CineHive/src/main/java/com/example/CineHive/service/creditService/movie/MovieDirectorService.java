package com.example.CineHive.service.creditService.movie;

import com.example.CineHive.entity.credit.movie.Director;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.videos.movie.MovieDirectorRepository;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MovieDirectorService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieDirectorRepository directorRepository;

    public MovieDirectorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    @Transactional
    public void saveMovieDirectors(Long movieId) {
        String response = webClient.get()
                .uri("/movie/" + movieId + "/credits?api_key=" + apiKey + "&language=ko")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode crewNode = rootNode.path("crew");

                Movie movie = movieRepository.findById(movieId).orElse(null);
                if (movie != null) {
                    for (JsonNode crewMember : crewNode) {
                        // 감독 정보를 찾기 위해 "job" 속성이 "Director"인 경우만 필터링
                        if ("Director".equals(crewMember.get("job").asText())) {
                            String directorName = crewMember.get("name").asText();

                            Director director = directorRepository.findByName(directorName)
                                    .orElseGet(() -> {
                                        // 새로운 감독 객체 생성 및 저장
                                        Director newDirector = new Director();
                                        newDirector.setName(directorName);
                                        newDirector.setGender(crewMember.get("gender").asInt());
                                        newDirector.setJob(crewMember.get("job").asText());
                                        return directorRepository.save(newDirector);
                                    });

                            // Movie에 Director 추가
                            movie.setDirector(director);
                            break; // 감독은 한 명만 있으므로 루프 탈출
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
    }

}
