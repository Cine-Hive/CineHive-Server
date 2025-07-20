package com.example.CineHive.mapper;

import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.entity.post.Comment;

public final class CommentMapper {

    private CommentMapper() {} // 유틸리티 클래스 인스턴스화 방지

    /**
     * Comment 엔티티를 CommentDto로 변환합니다.
     * 실제 변환 로직은 CommentDto의 fromEntity 정적 메서드에 위임합니다.
     */
    public static CommentDto toDto(Comment comment) {
        return CommentDto.fromEntity(comment);
    }
}