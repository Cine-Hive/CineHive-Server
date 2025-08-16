package com.example.CineHive.datasync.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * TMDB 동기화 배치 작업 모니터링 리스너
 * 작업 시작/종료 시점에 통계와 상태를 로깅
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbSyncJobListener implements JobExecutionListener {

    private final DataSource dataSource;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        LocalDateTime startTime = jobExecution.getStartTime();
        
        log.info("============================================");
        log.info("TMDB 동기화 배치 작업 시작");
        log.info("작업명: {}", jobName);
        log.info("시작 시간: {}", startTime.format(formatter));
        log.info("작업 ID: {}", jobExecution.getId());
        log.info("파라미터: {}", jobExecution.getJobParameters());
        log.info("============================================");
        
        // 큐 상태 확인
        logQueueStatus();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        Duration duration = Duration.between(startTime, endTime);
        
        log.info("============================================");
        log.info("TMDB 동기화 배치 작업 종료");
        log.info("작업명: {}", jobName);
        log.info("상태: {}", jobExecution.getStatus());
        log.info("종료 시간: {}", endTime.format(formatter));
        log.info("소요 시간: {}분 {}초", duration.toMinutes(), duration.toSecondsPart());
        log.info("종료 코드: {}", jobExecution.getExitStatus().getExitCode());
        
        // Step별 실행 결과
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        for (StepExecution stepExecution : stepExecutions) {
            logStepStatistics(stepExecution);
        }
        
        // 최종 큐 상태
        logQueueStatus();
        
        // 실패한 항목 로깅
        if (jobExecution.getStatus().isUnsuccessful()) {
            logFailedItems();
        }
        
        log.info("============================================");
    }
    
    private void logStepStatistics(StepExecution stepExecution) {
        log.info("--------------------------------------------");
        log.info("Step: {}", stepExecution.getStepName());
        log.info("  상태: {}", stepExecution.getStatus());
        log.info("  읽기: {} 건", stepExecution.getReadCount());
        log.info("  쓰기: {} 건", stepExecution.getWriteCount());
        log.info("  스킵: {} 건", stepExecution.getSkipCount());
        log.info("  커밋: {} 건", stepExecution.getCommitCount());
        log.info("  롤백: {} 건", stepExecution.getRollbackCount());
        
        if (!stepExecution.getFailureExceptions().isEmpty()) {
            log.error("  실패 예외:");
            stepExecution.getFailureExceptions().forEach(exception -> 
                log.error("    - {}: {}", exception.getClass().getSimpleName(), exception.getMessage())
            );
        }
    }
    
    private void logQueueStatus() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        try {
            // 전체 큐 상태
            Long totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tmdb_work_queue", Long.class);
            
            // 엔티티 타입별 통계
            String entityTypeSql = """
                SELECT entity_type, 
                       COUNT(*) as total,
                       SUM(CASE WHEN processed = true THEN 1 ELSE 0 END) as processed,
                       SUM(CASE WHEN processed = false THEN 1 ELSE 0 END) as pending,
                       SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed
                FROM tmdb_work_queue
                GROUP BY entity_type
                ORDER BY entity_type
            """;
            
            log.info("큐 상태 요약:");
            log.info("  전체 항목: {} 건", totalCount);
            
            jdbcTemplate.query(entityTypeSql, (rs, rowNum) -> {
                log.info("  {} - 전체: {}, 완료: {}, 대기: {}, 실패: {}",
                    rs.getString("entity_type"),
                    rs.getLong("total"),
                    rs.getLong("processed"),
                    rs.getLong("pending"),
                    rs.getLong("failed")
                );
                return null;
            });
            
        } catch (Exception e) {
            log.error("큐 상태 조회 실패: {}", e.getMessage());
        }
    }
    
    private void logFailedItems() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        try {
            String failedItemsSql = """
                SELECT entity_type, tmdb_id, last_error, retry_count
                FROM tmdb_work_queue
                WHERE status = 'FAILED'
                ORDER BY entity_type, tmdb_id
                LIMIT 10
            """;
            
            log.error("실패한 항목 (최대 10개):");
            jdbcTemplate.query(failedItemsSql, (rs, rowNum) -> {
                log.error("  [{} {}] 재시도: {}, 에러: {}",
                    rs.getString("entity_type"),
                    rs.getLong("tmdb_id"),
                    rs.getInt("retry_count"),
                    rs.getString("last_error")
                );
                return null;
            });
            
        } catch (Exception e) {
            log.error("실패 항목 조회 실패: {}", e.getMessage());
        }
    }
}