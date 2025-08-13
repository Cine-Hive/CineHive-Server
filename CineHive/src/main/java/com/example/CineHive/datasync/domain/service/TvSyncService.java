package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.datasync.dto.TvDelta;
import com.example.CineHive.datasync.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TvSyncService {

    private final TvSeriesRepository tvSeriesRepository;
    private final NetworkRepository networkRepository;
    private final TvGenreRepository tvGenreRepository;
    private final TvKeywordRepository tvKeywordRepository;
    private final TvCastRepository tvCastRepository;
    private final TvCrewRepository tvCrewRepository;
    private final TvSeriesNetworkRepository tvSeriesNetworkRepository;
    // TODO: TvSeriesProductionCompanyRepository 등 필요 시 주입

    @Transactional
    public void syncTvSeries(TvDelta delta) {
        Long tvId = delta.tvSeries().getTmdbId();

        if (delta.networks() != null && !delta.networks().isEmpty()) {
            networkRepository.saveAll(delta.networks());
        }

        tvSeriesRepository.save(delta.tvSeries());

        tvGenreRepository.deleteAllByTvId(tvId);
        if (delta.genres() != null && !delta.genres().isEmpty()) {
            tvGenreRepository.saveAll(delta.genres());
        }

        tvKeywordRepository.deleteAllByTvId(tvId);
        if (delta.keywords() != null && !delta.keywords().isEmpty()) {
            tvKeywordRepository.saveAll(delta.keywords());
        }

        tvCastRepository.deleteAllByTvId(tvId);
        if (delta.cast() != null && !delta.cast().isEmpty()) {
            tvCastRepository.saveAll(delta.cast());
        }

        tvCrewRepository.deleteAllByTvId(tvId);
        if (delta.crew() != null && !delta.crew().isEmpty()) {
            tvCrewRepository.saveAll(delta.crew());
        }

        tvSeriesNetworkRepository.deleteAllByTvId(tvId);
        if (delta.tvNetworks() != null && !delta.tvNetworks().isEmpty()) {
            tvSeriesNetworkRepository.saveAll(delta.tvNetworks());
        }
    }
}