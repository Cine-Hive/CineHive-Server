package com.example.CineHive.service.credit.drama;

import com.example.CineHive.entity.videotype.Drama;
import com.example.CineHive.entity.credit.drama.Genre;
import com.example.CineHive.repository.videos.drama.DramaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DramaService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private DramaRepository dramaRepository;

    @Autowired
    private DramaDirectorService dramaDirectorService;

    @Autowired
    private DramaActorService dramaActorService;

    @Autowired
    private DramaGenreService dramaGenreService;

    public DramaService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    public List<Drama> searchDramas(String query) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/search/tv?query="
                        + UriUtils.encode(query, StandardCharsets.UTF_8)
                        + "&api_key=" + apiKey
                        + "&include_adult=true&language=ko&page=1")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();  // block()을 사용하여 응답을 기다립니다.

        List<Drama> dramas = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode dramasNode = rootNode.path("results");

                for (JsonNode dramaNode : dramasNode) {
                    Long dramaId = dramaNode.get("id").asLong();
                    String posterPath = dramaNode.get("poster_path").asText();
                    String backDropPath = dramaNode.get("backdrop_path").asText();
                    Drama drama = new Drama();
                    drama.setId(dramaId);
                    drama.setName(dramaNode.get("name").asText());
                    drama.setOverview(dramaNode.get("overview").asText());

                    // 포스터 이미지가 없으면 다음 데이터로 넘어감
                    if (posterPath == null || posterPath.isEmpty()) {
                        continue;
                    }
                    drama.setPosterPath(posterPath);
                    drama.setBackDropPath(backDropPath);

                    // 장르 정보 설정
                    List<Genre> genres = new ArrayList<>();
                    boolean isAnimationGenre = false;
                    for (JsonNode genreIdNode : dramaNode.get("genre_ids")) {
                        Genre genre = new Genre();
                        genre.setId(genreIdNode.asInt());
                        genre.setName(dramaGenreService.getGenreNameById(genreIdNode.asInt()));
                        genres.add(genre);


                        if (genreIdNode.asInt() == 16) {
                            isAnimationGenre = true;
                        }
                    }
                    drama.setGenres(genres);


                    if (isAnimationGenre) {
                        System.out.println("드라마 '" + drama.getName() + "'은 애니메이션 장르로 제외됩니다.");
                        continue;
                    }

                    drama.setVoteAverage(dramaNode.get("vote_average").asDouble());
                    drama.setPopularity(dramaNode.get("popularity").asDouble());

                    String releaseDateString = dramaNode.get("first_air_date").asText();
                    if (!releaseDateString.isEmpty()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                        drama.setFirstAirDate(releaseDate.toString());
                    }

                    // 드라마가 이미 존재하는지 확인
                    if (!dramaRepository.existsById(dramaId)) {
                        dramaRepository.save(drama);
                        System.out.println("Saved new drama: " + drama.getName());
                    } else {
                        System.out.println("Drama already exists: " + drama.getName());
                    }

                    // 배우 정보 저장
                    dramaActorService.saveDramaCredits(dramaId);
                    // 감독 정보 저장
                    dramaDirectorService.saveDramaDirectors(dramaId);
                    // 데이터베이스와 상관없이 항상 리스트에 추가
                    dramas.add(drama);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return dramas;
    }
}
