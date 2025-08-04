package com.example.CineHive.domain.person.entity;

import com.example.CineHive.client.tmdb.dto.TmdbPersonDetailResponse;
import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;

/**
 * 인물(배우, 감독 등) 정보를 나타내는 엔티티입니다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "persons", indexes = {
        @Index(name = "idx_person_tmdb_id", columnList = "tmdbId", unique = true)
})
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE persons SET deleted_at = CURRENT_TIMESTAMP WHERE person_id = ?")
public class Person extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String name;

    private String profilePath;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Builder
    public Person(Long tmdbId, String name, String profilePath) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.profilePath = profilePath;
    }

    /**
     * TMDB API 응답 DTO를 Person 엔티티로 변환하는 정적 팩토리 메서드입니다.
     * 이 메서드는 findOrCreate 로직에서 TMDB로부터 새로운 인물 정보를 받아올 때 사용됩니다.
     * @param dto TmdbPersonDetailResponse 객체
     * @return Person 엔티티
     */
    public static Person from(TmdbPersonDetailResponse dto) {
        return Person.builder()
                .tmdbId(dto.id())
                .name(dto.name())
                .profilePath(dto.profilePath())
                .build();
    }

    /**
     * 인물 프로필 정보를 업데이트합니다.
     * @param name 새로운 이름
     * @param profilePath 새로운 프로필 이미지 경로
     */
    public void updateProfile(String name, String profilePath) {
        this.name = name;
        this.profilePath = profilePath;
    }
}