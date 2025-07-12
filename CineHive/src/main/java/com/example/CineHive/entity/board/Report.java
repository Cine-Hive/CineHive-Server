package com.example.CineHive.entity.board;

import com.example.CineHive.entity.BaseEntity;
import com.example.CineHive.entity.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reports")
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter; // 신고자 (user -> reporter)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id") // nullable = true
    private Board board; // 신고된 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id") // nullable = true
    private Comment comment; // 신고된 댓글

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Builder
    public Report(Member reporter, Board board, Comment comment, String reason) {
        this.reporter = reporter;
        this.board = board;
        this.comment = comment;
        this.reason = reason;
        this.status = ReportStatus.PENDING; // 생성 시 기본 상태는 '대기 중'
    }

    public void accept() {
        this.status = ReportStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ReportStatus.REJECTED;
    }
}