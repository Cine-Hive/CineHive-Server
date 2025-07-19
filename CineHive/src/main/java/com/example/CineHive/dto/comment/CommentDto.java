package com.example.CineHive.dto.comment;

import com.example.CineHive.entity.board.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 댓글 정보를 클라이언트에 전달하기 위한 응답 DTO입니다.
 */
@Schema(description = "댓글 정보 응답 DTO")
@Builder
public record CommentDto(
        @Schema(description = "댓글 고유 ID")
        Long id,

        @Schema(description = "댓글이 속한 게시글 ID")
        Long boardId,

        @Schema(description = "댓글 작성자 정보")
        CommenterInfo commenter,

        @Schema(description = "댓글 내용")
        String content,

        @Schema(description = "댓글 생성 시각")
        LocalDateTime createdAt
) {
    /**
     * 댓글 작성자의 정보를 담는 내부 DTO입니다.
     * 민감한 정보(이메일 등)는 제외하고 필요한 정보만 노출합니다.
     */
    @Schema(description = "댓글 작성자 정보")
    @Builder
    public record CommenterInfo(
            @Schema(description = "작성자 회원 ID")
            Long memberId,

            @Schema(description = "작성자 닉네임")
            String nickname
    ) {}

    /**
     * Comment 엔티티를 CommentDto로 변환하는 정적 팩토리 메서드입니다.
     * 이 메서드는 CommentMapper에서 사용됩니다.
     * @param comment 변환할 Comment 엔티티
     * @return 변환된 CommentDto
     */
    public static CommentDto fromEntity(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommenterInfo commenterInfo = CommenterInfo.builder()
                .memberId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .build();

        return CommentDto.builder()
                .id(comment.getId())
                .boardId(comment.getBoard().getId())
                .commenter(commenterInfo)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}