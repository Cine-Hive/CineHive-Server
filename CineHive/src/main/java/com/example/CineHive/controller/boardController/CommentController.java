package com.example.CineHive.controller.boardController;

import com.example.CineHive.dto.board.CommentDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.service.board.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /* 댓글 추가 */
    @PostMapping("/{boardId}/{memEmail}")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long boardId,
            @PathVariable String memEmail,
            @RequestBody CommentDto commentDto) {
        CommentDto createdComment = commentService.addComment(boardId, memEmail, commentDto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /* 댓글 조회 */
    @GetMapping("/all/board/{boardId}")
    public ResponseEntity<List<CommentDto>> getCommnetsByBoard(@PathVariable Long boardId){
        List<CommentDto> comments = commentService.getCommentsByBoard(boardId);
        return ResponseEntity.ok(comments);
    }

    /* 댓글 삭제 */

    @DeleteMapping("/board/{boardId}/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long boardId, @PathVariable Long commentId) {
        commentService.deleteComment(boardId, commentId);
        return ResponseEntity.noContent().build();
    }
}
