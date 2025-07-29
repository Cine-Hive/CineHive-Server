package com.example.CineHive.domain.post.comment;

import com.example.CineHive.domain.post.comment.dto.CommentResponse;
import com.example.CineHive.domain.post.comment.dto.CreateCommentRequest;
import com.example.CineHive.domain.post.comment.dto.UpdateCommentRequest;

import java.util.List;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스의 인터페이스입니다.
 */
public interface CommentService {

    /**
     * 특정 게시글에 새로운 댓글을 추가합니다.
     *
     * @param postId      댓글을 추가할 게시글의 ID
     * @param request     생성할 댓글의 내용이 담긴 DTO
     * @param userEmail   댓글을 작성하는 사용자의 이메일
     * @return 생성된 댓글의 정보를 담은 DTO
     */
    CommentResponse addComment(Long postId, CreateCommentRequest request, String userEmail);

    /**
     * 특정 게시글에 달린 모든 댓글을 조회합니다.
     *
     * @param postId 댓글 목록을 조회할 게시글의 ID
     * @return 해당 게시글의 댓글 DTO 리스트
     */
    List<CommentResponse> getCommentsByPost(Long postId);

    /**
     * 기존 댓글의 내용을 수정합니다.
     *
     * @param commentId   수정할 댓글의 ID
     * @param request     수정할 댓글의 내용이 담긴 DTO
     * @param userEmail   댓글을 수정하려는 사용자의 이메일
     * @return 수정된 댓글의 정보를 담은 DTO
     */
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String userEmail);

    /**
     * 특정 댓글을 삭제합니다.
     *
     * @param commentId   삭제할 댓글의 ID
     * @param userEmail   댓글을 삭제하려는 사용자의 이메일
     */
    void deleteComment(Long commentId, String userEmail);
}