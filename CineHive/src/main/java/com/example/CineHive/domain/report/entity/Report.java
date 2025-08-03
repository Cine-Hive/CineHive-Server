package com.example.CineHive.domain.report.controller.entity;

import com.example.CineHive.domain.post.comment.Comment;
import com.example.CineHive.domain.post.controller.Post;
import com.example.CineHive.domain.common.controller.BaseEntity;
import com.example.CineHive.domain.user.controller.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NamedEntityGraph(
        name = "Report.withAll",
        attributeNodes = {
                @NamedAttributeNode("reporter"),
                @NamedAttributeNode("post"),
                @NamedAttributeNode("comment")
        }
)
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
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Builder
    public Report(User reporter, Post post, Comment comment, String reason) {
        this.reporter = reporter;
        this.post = post;
        this.comment = comment;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
    }

    public void accept() {
        this.status = ReportStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ReportStatus.REJECTED;
    }
}
