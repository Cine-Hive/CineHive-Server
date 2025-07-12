package com.example.CineHive.entity.board;

import com.example.CineHive.entity.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글에 대한 북마크를 나타내는 엔티티입니다.
 * Member와 Board 사이의 다대다 관계를 해소하는 연결 테이블 역할을 합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "bookmarks",
        uniqueConstraints = {
                // 한 명의 회원은 하나의 게시글을 한 번만 북마크할 수 있도록 제약합니다.
                @UniqueConstraint(
                        name = "uk_member_board_bookmark",
                        columnNames = {"member_id", "board_id"}
                )
        }
)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // private User user; -> private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    /**
     * 북마크 생성을 위한 빌더입니다.
     * @param member 북마크하는 회원 엔티티
     * @param board 북마크의 대상이 되는 게시글 엔티티
     */
    @Builder
    public Bookmark(Member member, Board board) {
        this.member = member;
        this.board = board;
    }
}