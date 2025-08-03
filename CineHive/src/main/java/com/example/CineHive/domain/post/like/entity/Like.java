package com.example.CineHive.domain.post.controller.like;

import com.example.CineHive.domain.post.controller.Post;
import com.example.CineHive.domain.common.controller.BaseEntity;
import com.example.CineHive.domain.user.controller.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NamedEntityGraph(
        name = "PostLike.withUser",
        attributeNodes = @NamedAttributeNode("user")
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_post_like",
                        columnNames = {"user_id", "post_id"}
                )
        },
        indexes = {
                @Index(name = "idx_postlike_post_id", columnList = "post_id"),
                @Index(name = "idx_postlike_user_id", columnList = "user_id")
        }
)
public class Like extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}