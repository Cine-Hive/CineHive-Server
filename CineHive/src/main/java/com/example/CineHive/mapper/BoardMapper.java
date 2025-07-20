package com.example.CineHive.mapper;

import com.example.CineHive.dto.post.PostDto;
import com.example.CineHive.dto.post.PostSummaryDto;
import com.example.CineHive.entity.post.Post;

import java.util.stream.Collectors;

public final class BoardMapper {

    private BoardMapper() {}

    public static PostDto toBoardDto(Post post) {
        return PostDto.builder()
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

    public static PostSummaryDto toListDto(Post post) {
        return PostSummaryDto.builder()
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