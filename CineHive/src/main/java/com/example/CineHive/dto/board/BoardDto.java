package com.example.CineHive.dto.board;

import com.example.CineHive.dto.comment.CommentDto;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BoardDto(
        Long id,
        String brdTitle,
        String brdContent,
        String memNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int views,
        int likeCount,
        int dislikeCount,
        int bookmarkCount,
        List<CommentDto> comments
) {}