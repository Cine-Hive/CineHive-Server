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

    private String brdTitle;
    private String brdContent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime brdRegDate;

    private int views;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardLike> likes;

    @Column(name = "like_count", nullable = false, columnDefinition = "int default 0")
    private int likeCount;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardDisLike> dislikes;

    @Column(name = "dislike_count", nullable = false, columnDefinition = "int default 0")
    private int dislikeCount;

    private int reports;

    @ManyToOne
    @JoinColumn(name = "mem_id", referencedColumnName = "mem_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks;

    @Column(name = "bookmark_count", nullable = false, columnDefinition = "int default 0")
    private int bookmarkCount;

    @PrePersist
    protected void onCreate() {
        this.brdRegDate = LocalDateTime.now();
        this.views = 0;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.reports = 0;
        this.bookmarkCount = 0;
    }

    public int getBookmarkCount() {
        return bookmarkCount;
    }

    // 북마크 수 업데이트 메소드
    public void updateBookmarkCount() {
        this.bookmarkCount = this.bookmarks != null ? this.bookmarks.size() : 0;
    }

    public void updateLikeCount() {
        this.likeCount = this.likes != null ? this.likes.size() : 0;
    }

    public int getLikeCount() {
        return likeCount;
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

}
