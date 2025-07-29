package com.example.CineHive.domain.comment.dto;

import com.example.CineHive.domain.comment.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 댓글 정보 응답을 위한 DTO입니다.
 */
@Schema(description = "댓글 정보 응답")
@Builder
public record CommentResponse(
        @Schema(description = "댓글 고유 ID")
        Long id,

        @Schema(description = "댓글이 속한 게시글 ID")
        Long postId,

        @Schema(description = "댓글 작성자 정보")
        UserInfo commenter,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "댓글 생성 시각")
        LocalDateTime createdAt
) {
    /**
     * 댓글 작성자의 정보를 담는 DTO입니다.
     */
    @Schema(description = "댓글 작성자 정보")
    @Builder
    public record UserInfo(
            @Schema(description = "작성자 회원 ID")
            Long userId,

            @Schema(description = "작성자 닉네임")
            String nickname
    ) {}

    /**
     * Comment 엔티티를 CommentResponse DTO로 변환합니다.
     * @param comment 변환할 Comment 엔티티
     * @return 변환된 CommentResponse
     */
    public static CommentResponse from(Comment comment) {
        if (comment == null) {
            return null;
        }

        UserInfo commenterInfo = UserInfo.builder()
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .build();

        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .commenter(commenterInfo)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}