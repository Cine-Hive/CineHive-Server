<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/post/entity/Post.java
package com.example.CineHive.domain.post.entity;

import com.example.CineHive.domain.post.comment.entity.Comment;
import com.example.CineHive.domain.common.entity.BaseEntity;
import com.example.CineHive.domain.user.entity.User;
=======
package com.example.CineHive.domain.post;

import com.example.CineHive.domain.post.comment.Comment;
import com.example.CineHive.domain.common.BaseEntity;
import com.example.CineHive.domain.user.User;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/post/Post.java
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 엔티티입니다.
 * 모든 카운트(조회수, 좋아요 등)는 데이터 정합성을 위해 Repository 레벨의 원자적 업데이트 쿼리를 통해 관리됩니다.
 */
@NamedEntityGraph(
        name = "Post.withUser",
        attributeNodes = {
                @NamedAttributeNode("user")
        }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
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
    public Post(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
