package com.example.CineHive.domain.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "시청 가능한 플랫폼(OTT) 정보")
@Builder
public record WatchProviderInfo(
        @Schema(description = "해당 국가의 TMDB 링크")
        String link,
        @Schema(description = "구독 가능한 플랫폼 목록")
        List<ProviderOption> flatrate,
        @Schema(description = "대여 가능한 플랫폼 목록")
        List<ProviderOption> rent,
        @Schema(description = "구매 가능한 플랫폼 목록")
        List<ProviderOption> buy
) {
    /**
     * 개별 플랫폼 제공업체 정보
     */
    @Schema(description = "개별 플랫폼 제공업체 정보")
    @Builder
    public record ProviderOption(
            @Schema(description = "플랫폼 제공업체 ID")
            Long providerId,
            @Schema(description = "플랫폼 제공업체 이름")
            String providerName,
            @Schema(description = "플랫폼 로고 이미지 경로")
            String logoPath,
            @Schema(description = "표시 우선순위")
            Integer displayPriority // 필드 추가
    ) {}
}