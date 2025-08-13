package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncEpisodeCrew")
@Table(name = "episode_crew")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeCrew extends BaseEntity {
    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "episode_id", nullable = false)
    private Long episodeId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(nullable = false)
    private String job;

    private String department;

    @Builder
    public EpisodeCrew(String creditId, Long episodeId, Long personId, String job, String department) {
        this.creditId = creditId;
        this.episodeId = episodeId;
        this.personId = personId;
        this.job = job;
        this.department = department;
    }
}