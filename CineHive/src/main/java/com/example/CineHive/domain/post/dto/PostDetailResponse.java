package com.example.CineHive.domain.post.dto;

import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.domain.post.comment.dto.CommentResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String userNickname,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt,
        int views,
        int likeCount,
        int dislikeCount,
        int bookmarkCount,
        List<CommentResponse> comments
) {
    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .bookmarkCount(post.getBookmarkCount())
                .comments(post.getComments().stream()
                        .map(CommentResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}