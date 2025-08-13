package com.example.CineHive.datasync.domain.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TvSeriesNetworkId implements Serializable {
    private Long tvId;
    private Long networkId;
}