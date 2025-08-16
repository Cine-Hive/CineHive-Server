package com.example.CineHive.datasync.batch.reader;

import com.example.CineHive.datasync.dto.WorkQueueRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * FOR UPDATE SKIP LOCKED를 사용한 최적화된 워크큐 리더
 * 데드락과 중복 처리를 방지하면서 동시성을 극대화
 */
@Slf4j
public class OptimizedWorkQueueReader implements ItemReader<WorkQueueRow> {
    
    private final DataSource dataSource;
    private Iterator<WorkQueueRow> currentBatch;
    private final int batchSize;
    private final String entityType;
    
    public OptimizedWorkQueueReader(DataSource dataSource, String entityType, int batchSize) {
        this.dataSource = dataSource;
        this.entityType = entityType;
        this.batchSize = batchSize;
    }
    
    @Override
    public WorkQueueRow read() {
        // 현재 배치가 없거나 다 소진되었으면 새로운 배치를 가져옴
        if (currentBatch == null || !currentBatch.hasNext()) {
            currentBatch = claimNextBatch().iterator();
        }
        
        // 다음 아이템 반환
        if (currentBatch.hasNext()) {
            return currentBatch.next();
        }
        
        return null; // 더 이상 처리할 아이템이 없음
    }
    
    /**
     * FOR UPDATE SKIP LOCKED로 다음 배치를 원자적으로 가져오고 상태 변경
     */
    private List<WorkQueueRow> claimNextBatch() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        String sql = """
            WITH claimed AS (
                SELECT id, tmdb_id, priority
                FROM tmdb_work_queue
                WHERE entity_type = ?
                  AND status = 'PENDING'
                  AND (next_attempt_at IS NULL OR next_attempt_at <= NOW())
                ORDER BY priority DESC, tmdb_id
                FOR UPDATE SKIP LOCKED
                LIMIT ?
            )
            UPDATE tmdb_work_queue q
            SET status = 'PROCESSING',
                updated_at = NOW()
            FROM claimed
            WHERE q.id = claimed.id
            RETURNING q.entity_type, q.tmdb_id, q.priority, q.processed
        """;
        
        List<WorkQueueRow> batch = jdbcTemplate.query(sql, 
            new Object[]{entityType, batchSize},
            new WorkQueueRowMapper()
        );
        
        if (!batch.isEmpty()) {
            log.info("Claimed {} {} items for processing", batch.size(), entityType);
        }
        
        return batch;
    }
    
    /**
     * WorkQueueRow를 위한 RowMapper
     */
    private static class WorkQueueRowMapper implements RowMapper<WorkQueueRow> {
        @Override
        public WorkQueueRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new WorkQueueRow(
                rs.getString("entity_type"),
                rs.getLong("tmdb_id"),
                rs.getInt("priority"),
                rs.getBoolean("processed")
            );
        }
    }
}