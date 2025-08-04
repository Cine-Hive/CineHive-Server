<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/review/entity/Review.java
package com.example.CineHive.domain.review.entity;

import com.example.CineHive.domain.common.entity.BaseEntity;
import com.example.CineHive.domain.media.entity.Media;
import com.example.CineHive.domain.user.entity.User;
=======
package com.example.CineHive.domain.review;

import com.example.CineHive.domain.common.BaseEntity;
import com.example.CineHive.domain.media.Media;
import com.example.CineHive.domain.user.User;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/review/Review.java
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_media_review",
                        columnNames = {"user_id", "media_id"}
                )
        }
)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private double rating;

    @Builder
    public Review(User user, Media media, String content, double rating) {
        this.user = user;
        this.media = media;
        this.content = content;
        this.rating = rating;
    }

    public void update(String newContent, double newRating) {
        this.content = newContent;
        this.rating = newRating;
    }
}