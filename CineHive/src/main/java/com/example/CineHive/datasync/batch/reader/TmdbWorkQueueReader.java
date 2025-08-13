package com.example.CineHive.datasync.batch.reader;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbWorkQueueReader implements ItemReader<TmdbWorkQueue> {

    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;
    
    private Iterator<TmdbWorkQueue> currentIterator;
    private boolean hasMorePages = true;
    private int currentPage = 0;
    private final int pageSize = 100;

    @Override
    public TmdbWorkQueue read() throws Exception {
        if (currentIterator == null || (!currentIterator.hasNext() && hasMorePages)) {
            loadNextPage();
        }

        if (currentIterator != null && currentIterator.hasNext()) {
            return currentIterator.next();
        }

        return null; // 더 이상 읽을 데이터가 없음
    }

    private void loadNextPage() {
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize);
        List<TmdbWorkQueue> items = tmdbWorkQueueRepository.findUnprocessedByEntityType(
            TmdbWorkQueue.EntityType.MOVIE, 
            pageRequest
        );

        if (items.isEmpty()) {
            hasMorePages = false;
            currentIterator = null;
        } else {
            currentIterator = items.iterator();
            currentPage++;
            log.debug("TMDB Work Queue 페이지 로드: page={}, size={}", currentPage - 1, items.size());
        }
    }
}