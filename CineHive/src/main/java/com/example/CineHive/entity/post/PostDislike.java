package com.example.CineHive.entity.post;

import com.example.CineHive.entity.BaseEntity;
import com.example.CineHive.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NamedEntityGraph(
        name = "PostDislike.withUser",
        attributeNodes = @NamedAttributeNode("user")
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_dislikes")
public class PostDislike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public PostDislike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
