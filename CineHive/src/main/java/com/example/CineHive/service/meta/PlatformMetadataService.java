package com.example.CineHive.service.meta;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.dto.media.Platform;
import com.example.CineHive.dto.response.LogoDto;
import com.example.CineHive.dto.response.PlatformOptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformMetadataService {

    private final TmdbApiClient tmdbApiClient;

    /**
     * 모든 플랫폼의 상세 정보(모든 로고 포함)를 조회하고 결과를 캐싱합니다.
     * 이 메서드는 여러 번 호출되어도 캐시 덕분에 TMDB API를 반복적으로 호출하지 않습니다.
     * @return 모든 플랫폼의 옵션 DTO 목록
     */
    @Cacheable("platformMetadata")
    public Mono<List<PlatformOptionDto>> getPlatformOptions() {
        log.info("Fetching and caching all platform metadata from TMDB.");

        return Flux.fromArray(Platform.values())
                .flatMap(this::fetchPlatformDetails)
                .collectList();
    }

    /**
     * 단일 플랫폼에 대한 상세 정보를 TMDB에서 가져와 PlatformOptionDto로 변환합니다.
     * @param platform 정보를 가져올 Platform Enum 상수
     * @return 상세 정보가 포함된 PlatformOptionDto의 Mono
     */
    private Mono<PlatformOptionDto> fetchPlatformDetails(Platform platform) {
        return tmdbApiClient.getNetworkImages(platform.getId())
                .map(tmdbResponse -> {
                    List<LogoDto> logos = tmdbResponse.getLogos().stream()
                            .map(logo -> new LogoDto(logo.getFilePath(), logo.getFileType()))
                            .collect(Collectors.toList());

                    return new PlatformOptionDto(platform.name(), platform.getDisplayName(), logos);
                })
                .doOnError(e -> log.error("Failed to fetch details for platform: {}", platform.name(), e))
                .onErrorResume(e -> Mono.just(new PlatformOptionDto(platform.name(), platform.getDisplayName(), List.of())));
    }
}