package com.example.CineHive.entity.board;

import com.example.CineHive.entity.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글에 대한 '좋아요'를 나타내는 엔티티입니다.
 * Member와 Board 사이의 다대다 관계를 해소하는 연결 테이블 역할을 합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "board_likes",
        uniqueConstraints = {
                // 한 명의 회원은 하나의 게시글에 '좋아요'를 한 번만 누를 수 있도록 제약합니다.
                @UniqueConstraint(
                        name = "uk_member_board_like", // 제약 조건에 고유한 이름을 부여하는 것이 좋습니다.
                        columnNames = {"member_id", "board_id"}
                )
        }
)
public class BoardLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    /**
     * '좋아요' 생성을 위한 빌더입니다.
     * @param member '좋아요'를 누른 회원 엔티티
     * @param board '좋아요'의 대상이 되는 게시글 엔티티
     */
    @Builder
    public BoardLike(Member member, Board board) {
        this.member = member;
        this.board = board;
    }
}