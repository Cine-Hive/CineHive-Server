package com.example.CineHive.domain.post.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자의 북마크 상태 응답")
public record BookmarkStatusResponse(
        @Schema(description = "북마크 여부")
        boolean isBookmarked
) {}