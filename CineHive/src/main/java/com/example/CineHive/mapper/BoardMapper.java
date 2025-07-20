package com.example.CineHive.mapper;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.GetListBoardDto;
import com.example.CineHive.entity.post.Post;

import java.util.stream.Collectors;

public final class BoardMapper {

    private BoardMapper() {}

    public static BoardDto toBoardDto(Post post) {
        return BoardDto.builder()
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

    public static GetListBoardDto toListDto(Post post) {
        return GetListBoardDto.builder()
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