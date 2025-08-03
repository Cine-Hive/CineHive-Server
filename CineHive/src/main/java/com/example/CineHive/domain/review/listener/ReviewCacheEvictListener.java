package com.example.CineHive.domain.review.controller.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * 리뷰 변경 이벤트 발생 시, 관련 캐시를 무효화하는 리스너입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCacheEvictListener {

    private final CacheManager cacheManager;

    /**
     * 리뷰 데이터에 변경이 생기면(생성, 수정, 삭제), 'reviews' 캐시를 모두 삭제합니다.
     * @param event 리뷰 변경 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewChanged(ReviewChangedEvent event) {
        log.debug("'reviews' 캐시를 무효화합니다.");
        Objects.requireNonNull(cacheManager.getCache("reviews")).clear();
    }
}