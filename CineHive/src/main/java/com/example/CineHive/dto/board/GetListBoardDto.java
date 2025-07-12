package com.example.CineHive.dto.board;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record GetListBoardDto(
        Long id,
        String brdTitle,
        String memNickname,
        LocalDateTime createdAt,
        int views,
        int likeCount,
        int commentCount
) {}