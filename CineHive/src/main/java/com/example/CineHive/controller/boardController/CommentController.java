package com.example.CineHive.controller.boardController;

import com.example.CineHive.dto.board.CommentDto;
import com.example.CineHive.service.board.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/{boardId}/{memEmail}")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long boardId,
            @PathVariable String memEmail,
            @RequestBody CommentDto commentDto) {
        CommentDto createdComment = commentService.addComment(boardId, memEmail, commentDto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
}
