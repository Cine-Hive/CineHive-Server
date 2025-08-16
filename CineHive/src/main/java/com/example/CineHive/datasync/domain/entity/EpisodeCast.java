package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncEpisodeCast")
@Table(name = "episode_cast")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeCast extends BaseEntity {
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
    
    /**
     * TMDB API 응답을 EpisodeCast 엔티티로 변환하는 static factory 메서드
     */
    public static EpisodeCast fromTmdbResponse(Long episodeId, com.example.CineHive.client.tmdb.dto.TmdbMediaCastResponse response, 
                                              int castOrder, boolean isGuest) {
        return EpisodeCast.builder()
                .creditId(response.creditId())
                .episodeId(episodeId)
                .personId(response.id())
                .characterName(response.character())
                .castOrder(castOrder)
                .isGuest(isGuest)
                .build();
    }
}