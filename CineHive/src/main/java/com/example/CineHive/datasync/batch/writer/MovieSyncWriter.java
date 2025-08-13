package com.example.CineHive.datasync.batch.writer;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.service.MovieSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieSyncWriter implements ItemWriter<TmdbWorkQueue> {

    private final MovieSyncService movieSyncService;

    @Override
    public void write(Chunk<? extends TmdbWorkQueue> chunk) throws Exception {
        for (TmdbWorkQueue queueItem : chunk.getItems()) {
            try {
                movieSyncService.syncMovieFromQueue(queueItem);
            } catch (Exception e) {
                log.error("영화 동기화 실패: movieId={}, error={}", queueItem.getTmdbId(), e.getMessage());
                // 개별 아이템 실패는 전체를 중단시키지 않음 (faultTolerant 설정)
                throw e;
            }
        }
    }
}