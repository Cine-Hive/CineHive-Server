package com.example.CineHive.domain.media.entity;

import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "media_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_media_like", columnNames = {"user_id", "media_id"})
})
public class MediaLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Builder
    public MediaLike(User user, Media media) {
        this.user = user;
        this.media = media;
    }
}