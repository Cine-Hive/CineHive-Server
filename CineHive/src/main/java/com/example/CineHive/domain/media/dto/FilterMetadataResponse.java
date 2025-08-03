package com.example.CineHive.domain.media.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

@Schema(description = "미디어 검색/탐색 필터링을 위한 메타데이터 응답")
@Builder
public record FilterMetadataResponse(
        List<GenreOption> movieGenres,
        List<GenreOption> tvGenres,
        List<PlatformOption> platforms,
        List<SortOption> sortOptions
) {}