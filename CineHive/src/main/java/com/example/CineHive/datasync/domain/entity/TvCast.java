package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncTvCast")
@Table(name = "tv_cast")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvCast extends BaseEntity {
    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "tv_id", nullable = false)
    private Long tvId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "character_name", length = 512)
    private String characterName;

    @Column(name = "cast_order")
    private Integer castOrder;

    @Builder
    public TvCast(String creditId, Long tvId, Long personId, String characterName, Integer castOrder) {
        this.creditId = creditId;
        this.tvId = tvId;
        this.personId = personId;
        this.characterName = characterName;
        this.castOrder = castOrder;
    }
    
    /**
     * TMDB API 응답을 TvCast 엔티티로 변환하는 static factory 메서드
     */
    public static TvCast fromTmdbResponse(Long tvId, com.example.CineHive.client.tmdb.dto.TmdbMediaCastResponse response, int castOrder) {
        return TvCast.builder()
                .creditId(response.creditId())
                .tvId(tvId)
                .personId(response.id())
                .characterName(response.character())
                .castOrder(castOrder)
                .build();
    }
}