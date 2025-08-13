package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncTvSeasonCrew")
@Table(name = "tv_season_crew")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeasonCrew extends BaseEntity {
    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(nullable = false)
    private String job;

    private String department;

    @Builder
    public TvSeasonCrew(String creditId, Long seasonId, Long personId, String job, String department) {
        this.creditId = creditId;
        this.seasonId = seasonId;
        this.personId = personId;
        this.job = job;
        this.department = department;
    }
}