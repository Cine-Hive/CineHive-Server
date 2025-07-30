package com.example.CineHive.domain.credit;

import com.example.CineHive.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인물(감독, 배우 등) 정보를 나타내는 엔티티입니다.
 * TMDB의 인물 ID를 기본 키로 사용하여 중복 저장을 방지합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Person extends BaseEntity {

    @Id
    @Column(name = "person_id")
    private Long id; // TMDB 인물 ID를 그대로 사용

    @Column(nullable = false)
    private String name;

    private String profilePath;

    @Builder
    public Person(Long id, String name, String profilePath) {
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
    }
}