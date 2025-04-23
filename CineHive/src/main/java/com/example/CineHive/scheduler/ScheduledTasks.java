package com.example.CineHive.scheduler;

import com.example.CineHive.repository.ott.OttRepository;

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
    private OttRepository ottRepository;
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateMovies() {
        ottRepository.deleteAll();
        ottService.fetchAndSaveAllPlatformsMovies();

    }

}
