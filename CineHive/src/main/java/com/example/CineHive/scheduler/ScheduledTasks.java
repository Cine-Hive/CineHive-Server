package com.example.CineHive.scheduler;

import com.example.CineHive.service.credit.movie.NowPlayingMovieService;
import com.example.CineHive.service.credit.movie.PopularMovieService;
import com.example.CineHive.service.credit.movie.TopRatedMovieService;
import com.example.CineHive.service.credit.movie.UpComingMovieService;
import com.example.CineHive.service.ott.OttService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final OttService ottService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UpComingMovieService upComingMovieService;

    @Autowired
    private TopRatedMovieService topRatedMovieService;

    @Autowired
    private PopularMovieService popularMovieService;

    @Autowired
    private NowPlayingMovieService nowPlayingMovieService;
    @Scheduled(cron = "0 0 3 * * ?")
    public void updateMovies() {
        ottService.fetchAndSaveAllPlatformsMovies();
    }

    //개봉 예정 영화 자동 업데이트
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateUpcomingMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 개봉 예정 영화 업데이트 시작...");
        upComingMovieService.saveUpComingMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트]개봉 예정 영화 업데이트 완료!");
    }

    // 평점 순위 영화 자동 업데이트
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateTopRatedMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 시작...");
        topRatedMovieService.saveTopRatedMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 완료!");
    }

    // 인기 영화 자동 업데이트
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updatePopularMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 인기 영화 업데이트 시작...");
        popularMovieService.savePopularMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 인기 영화 업데이트 완료!");
    }

    // 현재 상영영화 자동 업데이트 (매일 자정)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateNowPlayingMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 시작...");
        nowPlayingMovieService.saveNowPlayingMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 완료!");
    }
}
