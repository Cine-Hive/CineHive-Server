package com.example.CineHive.domain.person.entity;

import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자와 인물 간의 '좋아요' 관계를 나타내는 엔티티입니다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "person_likes", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_person_like_user_person",
                columnNames = {"user_id", "person_id"}
        )
}, indexes = {
        @Index(name = "idx_person_like_person_id", columnList = "person_id")
})
public class PersonLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Builder
    public PersonLike(User user, Person person) {
        this.user = user;
        this.person = person;
    }
}