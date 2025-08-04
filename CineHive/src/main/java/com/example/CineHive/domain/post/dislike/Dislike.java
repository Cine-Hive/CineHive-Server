package com.example.CineHive.domain.post.dislike;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.global.entity.BaseEntity;
import com.example.CineHive.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NamedEntityGraph(
        name = "PostDislike.withUser",
        attributeNodes = @NamedAttributeNode("user")
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post_dislikes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_post_dislike",
                        columnNames = {"user_id", "post_id"}
                )
        },
        indexes = {
                @Index(name = "idx_postdislike_post_id", columnList = "post_id"),
                @Index(name = "idx_postdislike_user_id", columnList = "user_id")
        }
)
public class Dislike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public Dislike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}