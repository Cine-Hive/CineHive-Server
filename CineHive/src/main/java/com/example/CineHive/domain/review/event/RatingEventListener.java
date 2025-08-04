package com.example.CineHive.domain.review.event;

import com.example.CineHive.domain.review.repository.ReviewRepository;
import com.example.CineHive.global.util.DomainFinder;
import com.example.CineHive.domain.media.Media;
import com.example.CineHive.domain.media.MediaService;
import com.example.CineHive.domain.review.dto.RatingStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 리뷰 변경 이벤트를 수신하여 관련 비즈니스 로직을 비동기적으로 처리하는 리스너입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RatingEventListener {

    private final ReviewRepository reviewRepository;
    private final MediaService mediaService;
    private final DomainFinder domainFinder;

    /**
     * ReviewChangedEvent가 발생했을 때, 해당 미디어의 평점 통계를 비동기적으로 업데이트합니다.
     * 메인 트랜잭션이 성공적으로 커밋된 후에만 실행됩니다.
     * @param event 리뷰 변경 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReviewChanged(ReviewChangedEvent event) {
        log.debug("ReviewChangedEvent 수신 (after commit). 미디어 평점 업데이트 시작. mediaId: {}", event.getMediaId());
        try {
            Media media = domainFinder.findMediaById(event.getMediaId());
            RatingStats stats = reviewRepository.getRatingStatsByMedia(media);
            mediaService.updateMediaRating(media, stats);
        } catch (Exception e) {
            log.error("미디어 평점 비동기 업데이트 중 오류 발생. mediaId: {}", event.getMediaId(), e);
        }
    }
}