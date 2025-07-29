package com.example.CineHive.domain.post;

import com.example.CineHive.domain.post.dto.PostDetailResponse;
import com.example.CineHive.domain.post.dto.PostSummaryResponse;
import com.example.CineHive.domain.post.comment.CommentMapper; // comment 패키지로 이동 가정
import java.util.stream.Collectors;

/**
 * 게시글(Post) 엔티티를 DTO로 변환하는 유틸리티 클래스입니다.
 */
public final class PostMapper {

    private PostMapper() {}

    /**
     * Post 엔티티를 PostDetailResponse DTO로 변환합니다.
     * @param post 변환할 Post 엔티티
     * @return 변환된 PostDetailResponse
     */
    public static PostDetailResponse toDetailResponse(Post post) {
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
                        .map(CommentMapper::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Post 엔티티를 PostSummaryResponse DTO로 변환합니다.
     * @param post 변환할 Post 엔티티
     * @return 변환된 PostSummaryResponse
     */
    public static PostSummaryResponse toSummaryResponse(Post post) {
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