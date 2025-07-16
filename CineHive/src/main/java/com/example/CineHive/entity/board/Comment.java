package com.example.CineHive.entity.board;

import com.example.CineHive.entity.BaseEntity;
import com.example.CineHive.entity.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게시글에 대한 댓글을 나타내는 엔티티입니다.
 * 이 엔티티는 데이터와 자체 상태 변경 로직에만 집중합니다.
 * 비즈니스 규칙 검증(예: 권한 확인)은 서비스 레이어에서 처리합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 댓글 생성을 위한 빌더입니다.
     * @param content 댓글 내용
     * @param board 댓글이 달릴 게시글 엔티티
     * @param member 댓글을 작성한 회원 엔티티
     */
    @Builder
    public Comment(String content, Board board, Member member) {
        this.content = content;
        this.board = board;
        this.member = member;
    }

    //== 비즈니스 로직 (엔티티가 스스로의 상태를 관리) ==//

    /**
     * 댓글의 내용을 수정합니다.
     * @param newContent 새로운 댓글 내용
     */
    public void update(String newContent) {
        this.content = newContent;
    }
}
