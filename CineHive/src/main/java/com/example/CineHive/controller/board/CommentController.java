package com.example.CineHive.controller.board;

import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.service.board.CommentService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /* 댓글 추가 */
    @Operation(summary = "댓글 등록", description = "특정 게시글에 대한 댓글을 추가")
    @PostMapping("/{boardId}")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long boardId,
            @RequestBody CommentDto commentDto,
            HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            CommentDto createdComment = commentService.addComment(boardId, memEmail, commentDto.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    /* 댓글 조회 */
    @Operation(summary = "댓글 조회", description = "특정 게시글의 전채 댓글 수 조회")
    @GetMapping("/{boardId}/board/all")
    public ResponseEntity<List<CommentDto>> getCommentsByBoard(@PathVariable Long boardId) {
        List<CommentDto> comments = commentService.getCommentsByBoard(boardId);
        return ResponseEntity.ok(comments);
    }

    /* 댓글 삭제 */
    @Operation(summary = "댓글 삭제", description = "사용자가 등록한 특정 게시글의 댓글을 삭제")
    @DeleteMapping("/{boardId}/board/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long boardId, @PathVariable Long commentId, HttpServletRequest request) {
        // Authorization 헤더에서 토큰 추출
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            commentService.deleteComment(boardId, commentId, memEmail);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /* 댓글 수정 */
    @Operation(summary = "댓글 수정", description = "사용자가 등록한 특정 게시글의 댓글을 수정")
    @PutMapping("/{boardId}/board/update/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestBody CommentDto commentDto,
            HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            CommentDto updatedComment = commentService.updateComment(boardId, commentId, commentDto, memEmail);
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
