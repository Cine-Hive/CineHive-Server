package com.example.CineHive.scheduler;

import com.example.CineHive.service.ott.OttService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private final OttService ottService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void updateMovies() {
        ottService.fetchAndSaveAllPlatformsMovies();
    }
}
