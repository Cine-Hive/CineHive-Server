package com.example.CineHive.domain.person.entity;

import com.example.CineHive.global.entity.BaseEntity;
import com.example.CineHive.domain.media.entity.Media;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 미디어에 참여한 인물의 역할(크레딧)을 나타내는 엔티티입니다.
 * 예: '어벤져스(Media)'에 '조 루소(Person)'가 '감독(job)'으로 참여
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Credit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(nullable = false)
    private String job;

    private String characterName;

    @Builder
    public Credit(Media media, Person person, String job, String characterName) {
        this.media = media;
        this.person = person;
        this.job = job;
        this.characterName = characterName;
    }
}