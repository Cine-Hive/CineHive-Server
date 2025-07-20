package com.example.CineHive.mapper;

import com.example.CineHive.dto.post.PostDetailResponse;
import com.example.CineHive.dto.post.PostSummaryResponse;
import com.example.CineHive.entity.post.Post;

import java.util.stream.Collectors;

public final class BoardMapper {

    private BoardMapper() {}

    public static PostDetailResponse toBoardDto(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .brdTitle(post.getBrdTitle())
                .brdContent(post.getBrdContent())
                .memNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .bookmarkCount(post.getBookmarkCount())
                .comments(post.getComments().stream()
                        .map(CommentMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public static PostSummaryResponse toListDto(Post post) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .brdTitle(post.getBrdTitle())
                .memNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();
    }
}