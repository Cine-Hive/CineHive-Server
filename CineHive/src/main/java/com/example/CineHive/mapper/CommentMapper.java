package com.example.CineHive.mapper;

import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.entity.board.Comment;
import com.example.CineHive.exception.MappingException;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentDto toDTO(Comment comment) {

        if (comment == null) {
            throw new MappingException("Comment entity cannot be null");
        }
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getMemNickname(),
                comment.getUser().getMemEmail(),
                comment.getUser().getMemRegisterDatetime(),
                comment.getBoard().getId()
        );
    }

}
