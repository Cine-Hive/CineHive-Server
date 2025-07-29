package com.example.CineHive.domain.meta;

import com.example.CineHive.domain.media.dto.PlatformOption;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 플랫폼(OTT) 관련 메타데이터 조회 서비스의 명세를 정의하는 인터페이스입니다.
 */
public interface PlatformMetadataService {

    /**
     * 모든 플랫폼의 상세 정보(로고 포함)를 조회합니다.
     * 구현체는 이 결과를 캐싱하여 반복적인 API 호출을 방지해야 합니다.
     *
     * @return 모든 플랫폼의 옵션 DTO 목록을 담은 Mono
     */
    Mono<List<PlatformOption>> getPlatformOptions();
}
