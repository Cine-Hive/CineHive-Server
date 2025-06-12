package com.example.CineHive.service.media;

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
