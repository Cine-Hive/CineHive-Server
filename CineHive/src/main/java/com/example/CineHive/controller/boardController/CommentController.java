package com.example.CineHive.controller.boardController;

import com.example.CineHive.dto.board.CommentDto;
import com.example.CineHive.service.board.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment Controller", description = "게시글 댓글의 CURD 기능을 제공하는 API ")
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /* 댓글 추가 */
    @Operation(summary = "댓글 등록", description = "특정 게시글에 대한 댓글을 추가")
    @PostMapping("/{boardId}/{memEmail}")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long boardId,
            @PathVariable String memEmail,
            @RequestBody CommentDto commentDto) {
        CommentDto createdComment = commentService.addComment(boardId, memEmail, commentDto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /* 댓글 조회 */
    @Operation(summary = "댓글 조회", description = "특정 게시글의 전채 댓글 수 조회")
    @GetMapping("/all/board/{boardId}")
    public ResponseEntity<List<CommentDto>> getCommnetsByBoard(@PathVariable Long boardId){
        List<CommentDto> comments = commentService.getCommentsByBoard(boardId);
        return ResponseEntity.ok(comments);
    }

    /* 댓글 삭제 */
    @Operation(summary = "댓글 삭제", description = "사용자가 등록한 특정 게시글의 댓글을 삭제")
    @DeleteMapping("/board/{boardId}/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long boardId, @PathVariable Long commentId) {
        commentService.deleteComment(boardId, commentId);
        return ResponseEntity.noContent().build();
    }

    /* 댓글 수정 */
    @Operation(summary = "댓글 수정", description = "사용자가 등록한 특정 게시글의 댓글을 수정")
    @PutMapping("/board/{boardId}/update/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long boardId,
                                                    @PathVariable Long commentId,
                                                    @RequestBody CommentDto commentDto) {
        CommentDto updatedComment = commentService.updateComment(boardId, commentId, commentDto);
        return ResponseEntity.ok(updatedComment);
    }
}