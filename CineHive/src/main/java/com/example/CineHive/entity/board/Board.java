package com.example.CineHive.entity.board;

import com.example.CineHive.entity.BaseEntity;
import com.example.CineHive.entity.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글을 나타내는 엔티티입니다.
 * 이 엔티티는 데이터와 자체 상태 변경 로직에만 집중합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "boards")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String brdTitle;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String brdContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private int views = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int dislikeCount = 0;

    @Column(nullable = false)
    private int bookmarkCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Builder
    public Board(String brdTitle, String brdContent, Member member) {
        this.brdTitle = brdTitle;
        this.brdContent = brdContent;
        this.member = member;
    }

    //== 비즈니스 로직 (엔티티가 스스로의 상태를 관리) ==//

    public void update(String title, String content) {
        this.brdTitle = title;
        this.brdContent = content;
    }

    public void increaseViews() {
        this.views++;
    }

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount() {
        if (this.bookmarkCount > 0) {
            this.bookmarkCount--;
        }
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseDislikeCount() {
        this.dislikeCount++;
    }

    public void decreaseDislikeCount() {
        if (this.dislikeCount > 0) {
            this.dislikeCount--;
        }
    }

    public void updateCommentCount(int count) {
        this.commentCount = count;
    }
}
