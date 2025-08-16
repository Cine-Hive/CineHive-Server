package com.example.CineHive.datasync.batch.writer;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import com.example.CineHive.datasync.domain.service.PersonSyncService;
import com.example.CineHive.datasync.dto.PersonDelta;
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
public class PersonSyncWriter implements ItemWriter<PersonDelta> {

    private final PersonSyncService personSyncService;
    private final TmdbWorkQueueRepository workQueueRepository;

    @Override
    @Transactional
    public void write(Chunk<? extends PersonDelta> chunk) throws Exception {
        for (PersonDelta delta : chunk.getItems()) {
            if (delta == null) {
                continue;
            }
            
            Long tmdbId = delta.person().getTmdbId();
            
            try {
                // Sync person to database
                personSyncService.syncPerson(delta);
                
                // Mark as completed in work queue
                workQueueRepository.updateStatus(tmdbId, TmdbWorkQueue.EntityType.PERSON, TmdbWorkQueue.ProcessStatus.DONE);
                
                log.debug("Person synced successfully: tmdbId={}", tmdbId);
                
            } catch (TmdbClientException e) {
                if (e.getHttpStatus() == HttpStatus.TOO_MANY_REQUESTS) {
                    // Re-throw for retry mechanism
                    log.warn("Rate limit hit for person: tmdbId={}", tmdbId);
                    throw e;
                } else if (e.getHttpStatus() == HttpStatus.NOT_FOUND) {
                    // Mark as skipped for not found
                    workQueueRepository.updateStatus(tmdbId, TmdbWorkQueue.EntityType.PERSON, TmdbWorkQueue.ProcessStatus.SKIPPED);
                    log.info("Person not found, marked as skipped: tmdbId={}", tmdbId);
                } else {
                    // Mark as failed for other errors
                    workQueueRepository.updateStatusWithError(tmdbId, TmdbWorkQueue.EntityType.PERSON, 
                        TmdbWorkQueue.ProcessStatus.FAILED, truncateMessage(e.getMessage()));
                    log.error("Failed to sync person: tmdbId={}, error={}", tmdbId, e.getMessage());
                }
            } catch (Exception e) {
                // Mark as failed for unexpected errors
                workQueueRepository.updateStatusWithError(tmdbId, TmdbWorkQueue.EntityType.PERSON, 
                    TmdbWorkQueue.ProcessStatus.FAILED, truncateMessage(e.getMessage()));
                log.error("Unexpected error syncing person: tmdbId={}", tmdbId, e);
                // Don't re-throw to continue processing other items
            }
        }
    }
    
    private String truncateMessage(String message) {
        if (message == null) return null;
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}