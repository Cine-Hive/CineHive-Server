package com.example.CineHive.service.meta;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.dto.media.LogoInfo;
import com.example.CineHive.dto.media.Platform;
import com.example.CineHive.dto.media.PlatformOption;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PlatformMetadataService 인터페이스의 구현체입니다.
 * TMDB API 클라이언트를 사용하여 실제 메타데이터를 조회합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformMetadataServiceImpl implements PlatformMetadataService {

    private final TmdbApiClient tmdbApiClient;

    @Override
    @Cacheable("platformMetadata")
    public Mono<List<PlatformOption>> getPlatformOptions() {
        log.info("TMDB에서 모든 플랫폼 메타데이터를 조회하여 캐시에 저장합니다.");

        return Flux.fromArray(Platform.values())
                .flatMap(this::fetchPlatformDetails)
                .collectList()
                .onErrorMap(this::wrapClientException);
    }

    /**
     * 단일 플랫폼에 대한 상세 정보를 TMDB에서 가져와 PlatformOption으로 변환합니다.
     * @param platform 정보를 가져올 Platform Enum 상수
     * @return 상세 정보가 포함된 PlatformOption의 Mono
     */
    private Mono<PlatformOption> fetchPlatformDetails(Platform platform) {
        return tmdbApiClient.getNetworkImages(platform.getId())
                .map(tmdbResponse -> {
                    List<LogoInfo> logos = tmdbResponse.logos().stream()
                            .map(logo -> new LogoInfo(logo.filePath(), logo.fileType()))
                            .collect(Collectors.toList());

                    return new PlatformOption(platform.name(), platform.getDisplayName(), logos);
                });
    }

    /**
     * 외부 API 클라이언트에서 발생한 예외를 BusinessException으로 래핑합니다.
     * @param e 발생한 예외
     * @return BusinessException
     */
    private BusinessException wrapClientException(Throwable e) {
        if (e instanceof BusinessException) {
            return (BusinessException) e;
        }
        log.error("TMDB API 클라이언트에서 오류가 발생했습니다.", e);
        return new BusinessException("TMDB API Error: " + e.getMessage(), ErrorCode.TMDB_API_ERROR);
    }
}