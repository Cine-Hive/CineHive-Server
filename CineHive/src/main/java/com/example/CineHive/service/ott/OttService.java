package com.example.CineHive.service.ott;

import com.example.CineHive.dto.ott.MovieResponseDto;
import com.example.CineHive.dto.ott.OttDto;
import com.example.CineHive.entity.ott.Ott;
import com.example.CineHive.entity.ott.Provider;
import com.example.CineHive.mapper.OttMapper;
import com.example.CineHive.repository.ott.OttRepository;
import com.example.CineHive.repository.ott.ProviderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OttService {

    @Autowired
    private final WebClient.Builder webClientBuilder;

    @Autowired
    private final OttRepository ottRepository;

    @Autowired
    private final ProviderRepository providerRepository;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie";


    private static final Map<String, Integer> DEFAULT_PROVIDERS = Map.of(
            "netflix", 8,
            "disney", 337,
            "watcha", 97,
            "tving", 356
    );

    @PostConstruct
    @Transactional
    public void initializeProviders() {
        System.out.println(" initializeProviders() 실행됨");
        if (providerRepository.count() == 0) {
            DEFAULT_PROVIDERS.forEach((name, id) -> {
                providerRepository.findById(id).orElseGet(() -> {
                    System.out.println(" Provider 저장: " + name + " (ID: " + id + ")");
                    Provider provider = new Provider(id, name, null);
                    return providerRepository.save(provider);
                });
            });

            fetchAndSaveAllPlatformsMovies();
        } else {
            System.out.println("데이터 존재하므로 초기화 생략");
        }
    }
    
    public CompletableFuture<List<OttDto>> fetchAndSavePopularMovies(int providerId, String providerName) {
        WebClient webClient = webClientBuilder.baseUrl(TMDB_BASE_URL).build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("api_key", tmdbApiKey)
                        .queryParam("watch_region", "KR")
                        .queryParam("with_watch_providers", providerId)
                        .queryParam("sort_by", "popularity.desc")
                        .build())
                .retrieve()
                .bodyToMono(MovieResponseDto.MovieList.class)
                .map(response -> saveMovies(response.getResults(), providerId, providerName))
                .map(OttMapper::toDtoList)
                .toFuture();
    }

    @Transactional
    public void fetchAndSaveAllPlatformsMovies() {
        List<CompletableFuture<List<OttDto>>> futures = DEFAULT_PROVIDERS.entrySet().stream()
                .map(entry -> {
                    int providerId = entry.getValue();
                    String providerName = entry.getKey();
                    return fetchAndSavePopularMovies(providerId, providerName);
                })
                .collect(Collectors.toList());

        // 비동기 작업
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // TMDB에서 가져온 데이터를 DB에 저장
    private List<Ott> saveMovies(List<MovieResponseDto> movies, int providerId, String providerName) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerId));

        List<Ott> movieEntities = movies.stream()
                .map(dto -> new Ott(
                        null,
                        dto.getTitle(),
                        dto.getOverview(),
                        dto.getPosterPath(),
                        dto.getPopularity(),
                        dto.getReleaseDate(),
                        provider
                ))
                .collect(Collectors.toList());

        return ottRepository.saveAll(movieEntities);
    }

    // 특정 OTT의 인기 영화 조회 (DB에서 가져옴)
    public List<OttDto> getMoviesByProvider(int providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerId));
        return OttMapper.toDtoList(ottRepository.findByProvider(provider));
    }
}
