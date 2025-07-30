package com.example.CineHive.domain.post.dto;

import com.example.CineHive.domain.post.Post;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PostSummaryResponse(
        Long id,
        String title,
        String userNickname,
        LocalDateTime createdAt,
        int views,
        int likeCount,
        int commentCount
) {
    /**
     * Post 엔티티를 PostSummaryResponse DTO로 변환하는 정적 팩토리 메서드입니다.
     * @param post 변환할 Post 엔티티
     * @return 변환된 PostSummaryResponse
     */
    public static PostSummaryResponse from(Post post) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .userNickname(post.getUser().getNickname())
                .createdAt(post.getCreatedAt())
                .views(post.getViews())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();
    }
}
