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
    
    /**
     * TMDB API 응답을 EpisodeCrew 엔티티로 변환하는 static factory 메서드
     */
    public static EpisodeCrew fromTmdbResponse(Long episodeId, com.example.CineHive.client.tmdb.dto.TmdbMediaCrewResponse response) {
        return EpisodeCrew.builder()
                .creditId(response.creditId())
                .episodeId(episodeId)
                .personId(response.id())
                .job(response.job())
                .department(response.department())
                .build();
    }
}