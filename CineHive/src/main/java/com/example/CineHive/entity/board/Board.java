package com.example.CineHive.entity.board;

import com.example.CineHive.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private int likes;
    private int dislikes;
    private int reports;

    @ManyToOne
    @JoinColumn(name = "mem_id", referencedColumnName = "mem_id", nullable = false)
    private User user;

    private int BookmarkCount;

    @Column(name = "like_count")
    private Integer like;
    @PrePersist
    protected void onCreate() {
        this.brdRegDate = LocalDateTime.now();
        this.views = 0;
        this.likes = 0;
        this.dislikes = 0;
        this.reports = 0;
    }

    public void incrementFavoriteCount() {
        this.BookmarkCount++;
    }

    public void decrementFavoriteCount() {
        this.BookmarkCount--;
    }

}
