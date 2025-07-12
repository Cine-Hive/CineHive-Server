package com.example.CineHive.entity.board;

import com.example.CineHive.entity.BaseEntity;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.SelfReportException;
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

    @CreatedDate // 엔티티가 처음 저장될 때 시각을 자동으로 주입합니다.
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

    /**
     * 이 댓글의 소유주가 맞는지 확인합니다.
     * @param memberToVerify 확인할 회원 엔티티
     * @throws IllegalStateException 소유주가 아닐 경우 발생
     */
    public void verifyOwnership(Member memberToVerify) {
        // ID를 비교하는 것이 객체 자체를 비교하는 것보다 더 안전하고 명확합니다.
        if (!this.member.getId().equals(memberToVerify.getId())) {
            throw new IllegalStateException("이 댓글에 대한 권한이 없습니다.");
        }
    }

    /**
     * 신고자가 댓글 작성자 본인인지 검증합니다.
     * @param reporter 신고자 Member 엔티티
     * @throws SelfReportException 신고자와 작성자가 동일할 경우
     */
    public void validateNotSelfReport(Member reporter) {
        if (this.member.equals(reporter)) {
            throw new SelfReportException();
        }
    }
}