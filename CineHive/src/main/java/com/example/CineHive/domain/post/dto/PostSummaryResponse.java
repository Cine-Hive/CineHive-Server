package com.example.CineHive.domain.post.dto;

<<<<<<< HEAD
import com.example.CineHive.domain.post.entity.Post;
=======
import com.example.CineHive.domain.post.Post;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리)
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.time.Instant;

@Builder
public record PostSummaryResponse(
        Long id,
        String title,
        String userNickname,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,
        int views,
        int likeCount,
        int commentCount
) {
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