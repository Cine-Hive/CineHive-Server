package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.client.tmdb.dto.TmdbPersonDetailResponse;
import com.example.CineHive.datasync.domain.enums.GenderType;
import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity(name = "SyncPerson")
@Table(name = "person")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Person extends BaseEntity {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(length = 1024)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String biography;

    private LocalDate birthday;
    private LocalDate deathday;

    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(precision = 10, scale = 4)
    private BigDecimal popularity;

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    @Column(name = "soft_deleted_at")
    private ZonedDateTime softDeletedAt;

    @Column(name = "updated_from_tmdb_at")
    private ZonedDateTime updatedFromTmdbAt;

    @Builder
    public Person(Long tmdbId, String name, String biography, LocalDate birthday, LocalDate deathday, GenderType gender, String profilePath, BigDecimal popularity, ZonedDateTime updatedFromTmdbAt) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.biography = biography;
        this.birthday = birthday;
        this.deathday = deathday;
        this.gender = gender;
        this.profilePath = profilePath;
        this.popularity = popularity;
        this.updatedFromTmdbAt = updatedFromTmdbAt;
    }

    public static Person from(TmdbPersonDetailResponse dto) {
        if (dto == null) return null;

        return Person.builder()
                .tmdbId(dto.id())
                .name(dto.name())
                .biography(dto.biography())
                .birthday(dto.birthday())
                .deathday(dto.deathday())
                .gender(GenderType.fromTmdbId(dto.gender()))
                .profilePath(dto.profilePath())
                .popularity(dto.popularity())
                .build();
    }
}