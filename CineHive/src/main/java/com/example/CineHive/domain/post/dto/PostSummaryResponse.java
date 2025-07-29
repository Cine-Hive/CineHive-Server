package com.example.CineHive.domain.post.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PostSummaryResponse(
        Long id,
        String title,
        String userNickname,
        LocalDateTime createdAt,
        int views,
        int likeCount,
        int commentCount
) {}