package com.example.CineHive.mapper;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.BoardSearchDto;
import com.example.CineHive.dto.board.CreateBoardDto;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.user.User;

import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    /* 게시글 CRUD */
    public static BoardDto convertToDto(Board board) {
        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setBrdTitle(board.getBrdTitle());
        dto.setBrdContent(board.getBrdContent());
        dto.setMemNickname(board.getUser().getMemNickname());
        dto.setMemEmail(board.getUser().getMemEmail());
        dto.setBrdRegDate(board.getBrdRegDate());
        dto.setBookmarkCount(board.getBookmarkCount());
        dto.setLikeCount(board.getLikeCount());
        dto.setDislikeCount(board.getDisLikeCount());
        dto.setReportCount(board.getReportCount());
        dto.setCommentCount(board.getCommentCount());
        dto.setViews(board.getViews());
        return dto;
    }

    /*게시글 전체 조회 */
    public static List<BoardDto> convertToDtoList(List<Board> boards) {
        return boards.stream()
                .map(BoardMapper::convertToDto)
                .collect(Collectors.toList());
    }

    public static BoardSearchDto convertToSearchDto(Board board) {
        BoardSearchDto dto = new BoardSearchDto();
        dto.setBrdTitle(board.getBrdTitle());
        dto.setBrdContent(board.getBrdContent());
        dto.setMemNickname(board.getUser().getMemNickname());
        dto.setId(board.getId());
        dto.setViews(board.getViews());
        dto.setBrdRegDate(board.getBrdRegDate());
        dto.setBrdRegDate(board.getBrdRegDate());
        dto.setLikeCount(board.getLikeCount());
        return dto;
    }

    public static Board toEntity(CreateBoardDto dto, User user) {
        Board board = new Board();
        board.setBrdTitle(dto.getBrdTitle());
        board.setBrdContent(dto.getBrdContent());
        board.setUser(user);
        return board;
    }
}
