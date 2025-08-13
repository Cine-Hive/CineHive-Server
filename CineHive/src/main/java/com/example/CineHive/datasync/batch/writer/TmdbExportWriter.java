package com.example.CineHive.datasync.batch.writer;

import com.example.CineHive.datasync.dto.TmdbExportItem;
import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbExportWriter implements ItemWriter<TmdbExportItem> {

    private final JdbcTemplate jdbcTemplate;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;

    @Override
    public void write(Chunk<? extends TmdbExportItem> chunk) throws Exception {
        List<? extends TmdbExportItem> items = chunk.getItems();
        
        if (items.isEmpty()) {
            return;
        }

        // 배치로 중복 없이 삽입 (ON CONFLICT DO NOTHING 방식)
        String sql = """
            INSERT INTO tmdb_work_queue (entity_type, tmdb_id, priority, status, created_at, attempts) 
            VALUES (?, ?, ?, 'READY', CURRENT_TIMESTAMP, 0)
            ON CONFLICT (entity_type, tmdb_id) DO NOTHING
            """;

        jdbcTemplate.batchUpdate(sql, items.stream()
                .filter(item -> !item.adult()) // adult == true 스킵
                .map(item -> new Object[]{
                    "movie",  // entity_type
                    item.id(),
                    calculatePriority(item.popularity())
                })
                .toList());

        int processedCount = (int) items.stream()
                .filter(item -> !item.adult())
                .count();
        
        int skippedCount = items.size() - processedCount;

        log.debug("TMDB Export 시딩 완료: processed={}, skipped={}", processedCount, skippedCount);
    }
    
    private int calculatePriority(Double popularity) {
        if (popularity == null) return 0;
        
        // Convert popularity to priority (higher popularity = higher priority)
        // Scale: 0-100 popularity -> 0-1000 priority
        return Math.min((int) (popularity * 10), 1000);
    }
}