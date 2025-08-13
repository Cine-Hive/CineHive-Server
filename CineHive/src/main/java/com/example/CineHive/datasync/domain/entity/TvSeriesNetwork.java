package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity(name = "SyncTvSeriesNetwork")
@Table(name = "tv_series_network")
@IdClass(TvSeriesNetworkId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeriesNetwork extends BaseEntity {

    @Id
    @Column(name = "tv_id")
    private Long tvId;

    @Id
    @Column(name = "network_id")
    private Long networkId;

    @Builder
    public TvSeriesNetwork(Long tvId, Long networkId) {
        this.tvId = tvId;
        this.networkId = networkId;
    }
}