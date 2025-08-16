package com.example.CineHive.datasync.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Map;

/**
 * 워크큐 상태 관리 서비스
 * 재시도, 백오프, Stuck 처리 등을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueManagementService {
    
    private final DataSource dataSource;
    
    /**
     * 실패한 작업에 대한 지수 백오프 재시도 설정
     */
    @Transactional
    public void scheduleRetry(Long tmdbId, String entityType, int currentAttempt, String errorType) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        // 백오프 전략: 1분, 5분, 20분, 1시간, 4시간...
        Duration backoff = calculateBackoff(currentAttempt, errorType);
        
        String sql = """
            UPDATE tmdb_work_queue
            SET status = 'PENDING',
                attempts = ?,
                next_attempt_at = NOW() + INTERVAL ? SECOND,
                updated_at = NOW()
            WHERE tmdb_id = ? AND entity_type = ?
        """;
        
        jdbc.update(sql, currentAttempt + 1, backoff.getSeconds(), tmdbId, entityType);
        
        log.info("Scheduled retry for {} {} after {} seconds (attempt {})", 
            entityType, tmdbId, backoff.getSeconds(), currentAttempt + 1);
    }
    
    /**
     * 백오프 시간 계산
     */
    private Duration calculateBackoff(int attempt, String errorType) {
        // 429 (Rate Limit)는 더 긴 백오프
        if ("RATE_LIMIT".equals(errorType)) {
            return switch (attempt) {
                case 0 -> Duration.ofMinutes(5);
                case 1 -> Duration.ofMinutes(15);
                case 2 -> Duration.ofHours(1);
                default -> Duration.ofHours(4);
            };
        }
        
        // 일반 에러는 짧은 백오프
        return switch (attempt) {
            case 0 -> Duration.ofMinutes(1);
            case 1 -> Duration.ofMinutes(5);
            case 2 -> Duration.ofMinutes(20);
            case 3 -> Duration.ofHours(1);
            default -> Duration.ofHours(4);
        };
    }
    
    /**
     * 15분 이상 PROCESSING 상태인 작업을 PENDING으로 리셋
     * 매 5분마다 실행
     */
    @Scheduled(fixedDelay = 300000) // 5분
    @Transactional
    public void resetStuckItems() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        String sql = """
            UPDATE tmdb_work_queue
            SET status = 'PENDING',
                last_error = 'Reset from stuck state',
                updated_at = NOW()
            WHERE status = 'PROCESSING'
              AND updated_at < NOW() - INTERVAL '15 minutes'
        """;
        
        int updated = jdbc.update(sql);
        
        if (updated > 0) {
            log.warn("Reset {} stuck items from PROCESSING to PENDING", updated);
        }
    }
    
    /**
     * 큐 상태 통계 조회
     */
    public Map<String, Object> getQueueStatistics(String entityType) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        String sql = """
            SELECT 
                status,
                COUNT(*) as count,
                MIN(created_at) as oldest,
                MAX(updated_at) as newest,
                AVG(attempts) as avg_attempts
            FROM tmdb_work_queue
            WHERE entity_type = ?
            GROUP BY status
        """;
        
        return jdbc.queryForMap(sql, entityType);
    }
    
    /**
     * 실패한 작업들의 에러 분포 조회
     */
    public Map<String, Object> getErrorDistribution(String entityType, int limit) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        String sql = """
            SELECT 
                SUBSTRING(last_error, 1, 120) as error_summary,
                COUNT(*) as count
            FROM tmdb_work_queue
            WHERE entity_type = ?
              AND status = 'FAILED'
            GROUP BY SUBSTRING(last_error, 1, 120)
            ORDER BY COUNT(*) DESC
            LIMIT ?
        """;
        
        return jdbc.queryForMap(sql, entityType, limit);
    }
    
    /**
     * 404 에러는 영구 스킵 처리
     */
    @Transactional
    public void markAsSkipped(Long tmdbId, String entityType, String reason) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        String sql = """
            UPDATE tmdb_work_queue
            SET status = 'SKIPPED',
                last_error = ?,
                processed = true,
                updated_at = NOW()
            WHERE tmdb_id = ? AND entity_type = ?
        """;
        
        jdbc.update(sql, reason, tmdbId, entityType);
        
        log.info("Marked {} {} as SKIPPED: {}", entityType, tmdbId, reason);
    }
    
    /**
     * Advisory Lock을 사용한 중복 처리 방지
     */
    public boolean tryAcquireLock(Long tmdbId) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        Boolean locked = jdbc.queryForObject(
            "SELECT pg_try_advisory_lock(?)",
            Boolean.class,
            tmdbId
        );
        
        return Boolean.TRUE.equals(locked);
    }
    
    /**
     * Advisory Lock 해제
     */
    public void releaseLock(Long tmdbId) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("SELECT pg_advisory_unlock(" + tmdbId + ")");
    }
}