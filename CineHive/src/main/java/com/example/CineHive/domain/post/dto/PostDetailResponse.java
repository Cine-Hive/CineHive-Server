package com.example.CineHive.domain.post.dto;

import com.example.CineHive.domain.comment.dto.CommentResponse;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String userNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int views,
        int likeCount,
        int dislikeCount,
        int bookmarkCount,
        List<CommentResponse> comments
) {}