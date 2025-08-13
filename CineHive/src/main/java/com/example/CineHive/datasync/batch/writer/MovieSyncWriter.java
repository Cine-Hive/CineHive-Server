package com.example.CineHive.datasync.batch.writer;

import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import com.example.CineHive.datasync.domain.service.MovieSyncService;
import com.example.CineHive.datasync.dto.MovieDelta;
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
public class MovieSyncWriter implements ItemWriter<MovieDelta> {

    private final MovieSyncService movieSyncService;
    private final TmdbWorkQueueRepository workQueueRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends MovieDelta> chunk) throws Exception {
        for (MovieDelta delta : chunk.getItems()) {
            if (delta == null) {
                continue;
            }
            
            Long tmdbId = delta.movie().getTmdbId();
            
            try {
                // Sync movie to database
                movieSyncService.syncMovie(delta);
                
                // Mark as completed in work queue
                workQueueRepository.updateStatus(tmdbId, "MOVIE", "DONE");
                
                log.debug("Movie synced successfully: tmdbId={}", tmdbId);
                
            } catch (TmdbClientException e) {
                if (e.getHttpStatus() == HttpStatus.TOO_MANY_REQUESTS) {
                    // Re-throw for retry mechanism
                    log.warn("Rate limit hit for movie: tmdbId={}", tmdbId);
                    throw e;
                } else if (e.getHttpStatus() == HttpStatus.NOT_FOUND) {
                    // Mark as skipped for not found
                    workQueueRepository.updateStatus(tmdbId, "MOVIE", "SKIPPED");
                    log.info("Movie not found, marked as skipped: tmdbId={}", tmdbId);
                } else {
                    // Mark as failed for other errors
                    workQueueRepository.updateStatusWithError(tmdbId, "MOVIE", "FAILED", e.getMessage());
                    log.error("Failed to sync movie: tmdbId={}, error={}", tmdbId, e.getMessage());
                }
            } catch (Exception e) {
                // Mark as failed for unexpected errors
                workQueueRepository.updateStatusWithError(tmdbId, "MOVIE", "FAILED", e.getMessage());
                log.error("Unexpected error syncing movie: tmdbId={}", tmdbId, e);
                // Don't re-throw to continue processing other items
            }
        }
    }
}