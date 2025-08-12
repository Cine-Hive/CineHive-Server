package com.example.CineHive.datasync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ops/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;

    @Qualifier("initFullSyncJob")
    private final Job initFullSyncJob;

    @PostMapping("/start-full-sync")
    public ResponseEntity<?> startFullSyncJob() {
        try {
            String fileDate = LocalDate.now(ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.ofPattern("MM_dd_yyyy"));

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobName", "initFullSyncJob-" + fileDate)
                    .addString("fileDate", fileDate)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(initFullSyncJob, jobParameters);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "InitFullSyncJob has been started.");
            response.put("executionId", execution.getId());
            response.put("status", execution.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to start InitFullSyncJob.");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}