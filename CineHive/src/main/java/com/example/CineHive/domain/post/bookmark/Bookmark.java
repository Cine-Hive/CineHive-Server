package com.example.CineHive.domain.post.bookmark;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.global.common.BaseEntity;
import com.example.CineHive.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NamedEntityGraph(
        name = "Bookmark.withUser",
        attributeNodes = @NamedAttributeNode("user")
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "bookmarks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_post_bookmark",
                        columnNames = {"user_id", "post_id"}
                )
        },
        indexes = {
                @Index(name = "idx_bookmark_post_id", columnList = "post_id"),
                @Index(name = "idx_bookmark_user_id", columnList = "user_id")
        }
)
public class Bookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public Bookmark(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
