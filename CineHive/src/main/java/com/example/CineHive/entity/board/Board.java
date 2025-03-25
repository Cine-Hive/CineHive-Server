package com.example.CineHive.entity.board;

import com.example.CineHive.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/*
게시판 테이블
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memEmail;
    private String brdTitle;
    private String brdContent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime brdRegDate;

    private int views;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardLike> likes; //좋아요 리스트

    @Column(name = "like_count", nullable = false, columnDefinition = "int default 0")
    private int likeCount; // 특정 게시글의 좋아요 갯수

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardDisLike> dislikes; //싫어요 리스트

    @Column(name = "dislike_count", nullable = false, columnDefinition = "int default 0")
    private int dislikeCount; //특정 게시글의 싫어요 갯수

    @Column(name = "report_count", nullable = false, columnDefinition = "int default 0")
    private int reportCount; // 특정 게시글의 신고 갯수

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports; // 신고 목록


    @ManyToOne
    @JoinColumn(name = "mem_id", referencedColumnName = "mem_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks; // 즐겨찾기 리스트

    @Column(name = "bookmark_count", nullable = false, columnDefinition = "int default 0")
    private int bookmarkCount; // 즐겨찾기 총 수

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments; // 댓글 리스트

    @Column(name = "comment_count", nullable = false, columnDefinition = "int default 0")
    private int commentCount; // 게시글의 댓글 총 수

    @PrePersist
    protected void onCreate() {
        this.brdRegDate = LocalDateTime.now();
        this.views = 0;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.reportCount = 0;
        this.bookmarkCount = 0;
        this.commentCount = 0;
    }


    // 북마크 수 업데이트 메소드
    public void updateBookmarkCount() {
        this.bookmarkCount = this.bookmarks != null ? this.bookmarks.size() : 0;
    }

    public void updateLikeCount() {
        this.likeCount = this.likes != null ? this.likes.size() : 0;
    }

    public void updateDisLikeCount() {
        this.dislikeCount = this.dislikes != null ? this.dislikes.size() : 0;
    }

    public int getDisLikeCount() {
        return dislikeCount;
    }

    public void increaseViews() {
        this.views++;
    }

    public void increaseReportCount() {
        this.reportCount++;
    }
}
