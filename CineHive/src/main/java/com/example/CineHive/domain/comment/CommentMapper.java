package com.example.CineHive.domain.comment;

import com.example.CineHive.domain.comment.dto.CommentResponse;

/**
 * 댓글(Comment) 엔티티를 DTO로 변환하는 유틸리티 클래스입니다.
 */
public final class CommentMapper {

    private CommentMapper() {}

    /**
     * Comment 엔티티를 CommentResponse DTO로 변환합니다.
     * 실제 변환 로직은 CommentResponse의 fromEntity 정적 메서드에 위임합니다.
     *
     * @param comment 변환할 Comment 엔티티
     * @return 변환된 CommentResponse
     */
    public static CommentResponse toResponse(Comment comment) {
        return CommentResponse.from(comment);
    }
}