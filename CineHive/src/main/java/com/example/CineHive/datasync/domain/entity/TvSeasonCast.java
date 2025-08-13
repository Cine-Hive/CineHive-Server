package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncTvSeasonCast")
@Table(name = "tv_season_cast")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeasonCast extends BaseEntity {
    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "character_name", length = 512)
    private String characterName;

    @Column(name = "cast_order")
    private Integer castOrder;

    @Builder
    public TvSeasonCast(String creditId, Long seasonId, Long personId, String characterName, Integer castOrder) {
        this.creditId = creditId;
        this.seasonId = seasonId;
        this.personId = personId;
        this.characterName = characterName;
        this.castOrder = castOrder;
    }
}