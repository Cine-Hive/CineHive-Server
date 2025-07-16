package com.example.CineHive.service.media;

/**
 * MediaSyncService 미디어 동기화 관련 서비스입니다.
 * 실제 데이터베이스와 상호작용하며 미디어 동기화관련 비즈니스 로직을 수행합니다.
 */
public interface MediaSyncService {
    void syncPopularMovies(int page);
    void syncTopRatedTvSeries(int page);
    void syncUpcomingMovies(int page);
    void syncOnTheAirTvSeries(int page);
    void syncTopRatedMovies(int page);
    void syncPopularTvSeries(int page);
    void syncNowPlayingMovies(int page);
    void syncAiringTodayTvSeries(int page);
}
