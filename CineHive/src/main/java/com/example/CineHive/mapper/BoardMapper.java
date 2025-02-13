package com.example.CineHive.mapper;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.entity.board.Board;

import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    /* 게시글 등록 */
    public static BoardDto convertToDto(Board board) {
        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setBrdTitle(board.getBrdTitle());
        dto.setBrdContent(board.getBrdContent());
        dto.setNickname(board.getUser().getMemNickname());
        dto.setEmail(board.getUser().getMemEmail());
        dto.setBrgRedDate(board.getBrdRegDate());
        dto.setBookmarkcount(board.getBookmarkCount());
        return dto;
    }

    /*게시글 전체 조회 */
    public static List<BoardDto> convertToDtoList(List<Board> boards) {
        return boards.stream()
                .map(BoardMapper::convertToDto)
                .collect(Collectors.toList());
    }

}
