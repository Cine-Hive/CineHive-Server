package com.example.CineHive.dto.post;

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