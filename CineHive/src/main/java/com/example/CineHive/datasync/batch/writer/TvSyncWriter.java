package com.example.CineHive.datasync.batch.writer;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import com.example.CineHive.datasync.domain.service.TvSyncService;
import com.example.CineHive.datasync.dto.TvDelta;
import com.example.CineHive.global.exception.TmdbClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TvSyncWriter implements ItemWriter<TvDelta> {

    private final TvSyncService tvSyncService;
    private final TmdbWorkQueueRepository workQueueRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends TvDelta> chunk) throws Exception {
        for (TvDelta delta : chunk.getItems()) {
            if (delta == null) {
                continue;
            }
            
            Long tmdbId = delta.tvSeries().getTmdbId();
            
            try {
                // Sync TV series to database
                tvSyncService.syncTv(delta);
                
                // Mark as completed in work queue
                workQueueRepository.updateStatus(tmdbId, TmdbWorkQueue.EntityType.TV, TmdbWorkQueue.ProcessStatus.DONE);
                
                log.debug("TV series synced successfully: tmdbId={}", tmdbId);
                
            } catch (TmdbClientException e) {
                if (e.getHttpStatus() == HttpStatus.TOO_MANY_REQUESTS) {
                    // Re-throw for retry mechanism
                    log.warn("Rate limit hit for TV series: tmdbId={}", tmdbId);
                    throw e;
                } else if (e.getHttpStatus() == HttpStatus.NOT_FOUND) {
                    // Mark as skipped for not found
                    workQueueRepository.updateStatus(tmdbId, TmdbWorkQueue.EntityType.TV, TmdbWorkQueue.ProcessStatus.SKIPPED);
                    log.info("TV series not found, marked as skipped: tmdbId={}", tmdbId);
                } else {
                    // Mark as failed for other errors
                    workQueueRepository.updateStatusWithError(tmdbId, TmdbWorkQueue.EntityType.TV, 
                        TmdbWorkQueue.ProcessStatus.FAILED, truncateMessage(e.getMessage()));
                    log.error("Failed to sync TV series: tmdbId={}, error={}", tmdbId, e.getMessage());
                }
            } catch (Exception e) {
                // Mark as failed for unexpected errors
                workQueueRepository.updateStatusWithError(tmdbId, TmdbWorkQueue.EntityType.TV, 
                    TmdbWorkQueue.ProcessStatus.FAILED, truncateMessage(e.getMessage()));
                log.error("Unexpected error syncing TV series: tmdbId={}", tmdbId, e);
                // Don't re-throw to continue processing other items
            }
        }
    }
    
    private String truncateMessage(String message) {
        if (message == null) return null;
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}