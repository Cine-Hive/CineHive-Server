package com.example.CineHive.domain.meta;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.domain.media.dto.LogoInfo;
import com.example.CineHive.domain.media.dto.Platform;
import com.example.CineHive.domain.media.dto.PlatformOption;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformMetadataServiceImpl implements PlatformMetadataService {

    private final TmdbApiClient tmdbApiClient;

    @Override
    @Cacheable("platformMetadata")
    public List<PlatformOption> getPlatformOptions() {
        log.info("TMDB에서 모든 플랫폼 메타데이터를 조회하여 캐시에 저장합니다.");
        try {
            return Arrays.stream(Platform.values())
                    .parallel() // 병렬 스트림으로 여러 API를 동시에 호출하여 성능 향상
                    .map(this::fetchPlatformDetails)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw wrapClientException(e);
        }
    }

    private PlatformOption fetchPlatformDetails(Platform platform) {
        var tmdbResponse = tmdbApiClient.getNetworkImages(platform.getId());
        List<LogoInfo> logos = tmdbResponse.logos().stream()
                .map(logo -> new LogoInfo(logo.filePath(), logo.fileType()))
                .collect(Collectors.toList());
        return new PlatformOption(platform.name(), platform.getDisplayName(), logos);
    }

    private BusinessException wrapClientException(Throwable e) {
        if (e instanceof BusinessException) {
            return (BusinessException) e;
        }
        log.error("TMDB API 클라이언트에서 오류가 발생했습니다.", e);
        return new BusinessException("TMDB API Error: " + e.getMessage(), ErrorCode.TMDB_API_ERROR);
    }
}