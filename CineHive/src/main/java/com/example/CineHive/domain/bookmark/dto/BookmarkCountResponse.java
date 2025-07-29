package com.example.CineHive.domain.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크 개수 응답")
public record BookmarkCountResponse(
        @Schema(description = "북마크 개수")
        int count
) {}