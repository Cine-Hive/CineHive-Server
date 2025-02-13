package com.example.CineHive.mapper;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.entity.board.Board;

public class BoardMapper {

    public static BoardDto convertToDto(Board board) {
        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setBrdTitle(board.getBrdTitle());
        dto.setBrdContent(board.getBrdContent());
        dto.setNickname(board.getUser().getMemNickname());
        dto.setEmail(board.getUser().getMemEmail());
        dto.setBrgRedDate(board.getBrdRegDate());
        return dto;
    }
}
