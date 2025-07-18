package com.example.CineHive.mapper;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.GetListBoardDto;
import com.example.CineHive.entity.board.Board;

import java.util.stream.Collectors;

public final class BoardMapper {

    private BoardMapper() {}

    public static BoardDto toBoardDto(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .brdTitle(board.getBrdTitle())
                .brdContent(board.getBrdContent())
                .memNickname(board.getMember().getNickname())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .views(board.getViews())
                .likeCount(board.getLikeCount())
                .dislikeCount(board.getDislikeCount())
                .bookmarkCount(board.getBookmarkCount())
                .comments(board.getComments().stream()
                        .map(CommentMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public static GetListBoardDto toListDto(Board board) {
        return GetListBoardDto.builder()
                .id(board.getId())
                .brdTitle(board.getBrdTitle())
                .memNickname(board.getMember().getNickname())
                .createdAt(board.getCreatedAt())
                .views(board.getViews())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .build();
    }
}