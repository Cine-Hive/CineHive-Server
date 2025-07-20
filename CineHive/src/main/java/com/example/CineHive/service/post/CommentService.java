package com.example.CineHive.service.post;

import com.example.CineHive.dto.comment.CommentResponse;
import com.example.CineHive.dto.comment.CreateCommentRequest;
import com.example.CineHive.dto.comment.UpdateCommentRequest;

import java.util.List;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스의 인터페이스입니다.
 * 이 인터페이스는 댓글의 생성, 조회, 수정, 삭제 기능을 정의합니다.
 */
public interface CommentService {

    /**
     * 특정 게시물에 새로운 댓글을 추가합니다.
     *
     * @param boardId     댓글을 추가할 게시물의 ID
     * @param request     생성할 댓글의 내용이 담긴 DTO
     * @param memberEmail 댓글을 작성하는 회원의 이메일 (인증 정보)
     * @return 생성된 댓글의 정보를 담은 DTO
     */
    CommentResponse addComment(Long boardId, CreateCommentRequest request, String memberEmail);

    /**
     * 특정 게시물에 달린 모든 댓글을 조회합니다.
     *
     * @param boardId 댓글 목록을 조회할 게시물의 ID
     * @return 해당 게시물의 댓글 DTO 리스트
     */
    List<CommentResponse> getCommentsByBoard(Long boardId);

    /**
     * 기존 댓글의 내용을 수정합니다.
     *
     * @param commentId   수정할 댓글의 ID
     * @param request     수정할 댓글의 내용이 담긴 DTO
     * @param memberEmail 댓글을 수정하려는 회원의 이메일 (인증 및 소유권 검증용)
     * @return 수정된 댓글의 정보를 담은 DTO
     */
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String memberEmail);

    /**
     * 특정 댓글을 삭제합니다.
     *
     * @param commentId   삭제할 댓글의 ID
     * @param memberEmail 댓글을 삭제하려는 회원의 이메일 (인증 및 소유권 검증용)
     */
    void deleteComment(Long commentId, String memberEmail);
}