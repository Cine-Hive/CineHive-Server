package com.example.CineHive.domain.post.dto;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.post.comment.dto.CommentResponse;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String userNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int views,
        int likeCount,
        int dislikeCount,
        int bookmarkCount,
        List<CommentResponse> comments
) {
    /**
     * Post 엔티티를 PostDetailResponse DTO로 변환하는 정적 팩토리 메서드입니다.
     * @param post 변환할 Post 엔티티
     * @return 변환된 PostDetailResponse
     */
    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .bookmarkCount(post.getBookmarkCount())
                .comments(post.getComments().stream()
                        .map(CommentResponse::from) // CommentMapper 대신 CommentResponse.from 직접 호출
                        .collect(Collectors.toList()))
                .build();
    }
}
