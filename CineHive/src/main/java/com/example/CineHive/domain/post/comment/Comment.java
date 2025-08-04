<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/post/comment/entity/Comment.java
package com.example.CineHive.domain.post.comment.entity;

import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.domain.common.entity.BaseEntity;
import com.example.CineHive.domain.user.entity.User;
=======
package com.example.CineHive.domain.post.comment;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.common.BaseEntity;
import com.example.CineHive.domain.user.User;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/post/comment/Comment.java
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NamedEntityGraph(
        name = "Comment.withUser",
        attributeNodes = {
                @NamedAttributeNode("user")
        }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_post_id", columnList = "post_id"),
        @Index(name = "idx_comment_user_id", columnList = "user_id")
})
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Builder
    public Comment(String content, Post post, User user) {
        this.content = content;
        this.post = post;
        this.user = user;
    }

    public void update(String newContent) {
        this.content = newContent;
    }
}