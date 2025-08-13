package com.example.CineHive.datasync.dto;

import com.example.CineHive.datasync.domain.entity.*;
import java.util.List;

public record TvDelta(
        TvSeries tvSeries,
        List<TvGenre> genres,
        List<TvKeyword> keywords,
        List<TvCast> cast,
        List<TvCrew> crew,
        List<Network> networks,
        List<TvSeriesNetwork> tvNetworks
        // TODO: ProductionCompany 관련 필드도 필요 시 추가
) {}