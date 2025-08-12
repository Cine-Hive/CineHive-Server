package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncEpisodeCast")
@Table(name = "episode_cast")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeCast {
    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "episode_id", nullable = false)
    private Long episodeId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "character_name", length = 512)
    private String characterName;

    @Column(name = "cast_order")
    private Integer castOrder;

    private boolean isGuest;

    @Builder
    public EpisodeCast(String creditId, Long episodeId, Long personId, String characterName, Integer castOrder, boolean isGuest) {
        this.creditId = creditId;
        this.episodeId = episodeId;
        this.personId = personId;
        this.characterName = characterName;
        this.castOrder = castOrder;
        this.isGuest = isGuest;
    }
}