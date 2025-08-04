package com.example.CineHive.domain.review.service;

import com.example.CineHive.domain.review.repository.ReviewRepository;
import com.example.CineHive.domain.review.entity.Review;
import com.example.CineHive.domain.review.event.ReviewChangedEvent;
import com.example.CineHive.global.util.DomainFinder;
import com.example.CineHive.global.dto.SliceResponse;
import com.example.CineHive.domain.media.Media;
import com.example.CineHive.domain.media.MediaRepository;
import com.example.CineHive.domain.media.MediaService;
import com.example.CineHive.domain.media.MediaType;
import com.example.CineHive.domain.review.dto.CreateReviewRequest;
import com.example.CineHive.domain.review.dto.ReviewResponse;
import com.example.CineHive.domain.review.dto.UpdateReviewRequest;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.global.properties.ReviewProperties;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "reviews")
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MediaRepository mediaRepository;
    private final DomainFinder domainFinder;
    private final MediaService mediaService;
    private final ApplicationEventPublisher eventPublisher;
    private final ReviewProperties reviewProperties;

    @Override
    @Transactional
    public ReviewResponse createReview(Integer tmdbId, MediaType mediaType, CreateReviewRequest request, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Media media = mediaService.findOrCreateMedia(tmdbId, mediaType);

        if (reviewRepository.existsByUserAndMedia(user, media)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .user(user)
                .media(media)
                .content(request.content())
                .rating(request.rating())
                .build();

        Review savedReview = reviewRepository.save(review);
        eventPublisher.publishEvent(new ReviewChangedEvent(media.getId()));

        log.info("새로운 리뷰가 생성되었습니다. 리뷰 ID: {}", savedReview.getId());
        return ReviewResponse.from(savedReview);
    }

    @Override
    @Cacheable(key = "'tmdb:' + #tmdbId + ':' + #mediaType.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':sort:' + #pageable.sort")
    public SliceResponse<ReviewResponse> getReviewsForMedia(Integer tmdbId, MediaType mediaType, Pageable pageable) {
        log.debug("Cache miss. DB에서 리뷰 목록을 조회합니다. tmdbId: {}, mediaType: {}, pageable: {}", tmdbId, mediaType, pageable);
        Pageable adjustedPageable = adjustPageable(pageable);

        return mediaRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                .map(media -> {
                    Slice<Review> reviewSlice = reviewRepository.findByMedia(media, adjustedPageable);
                    return SliceResponse.from(reviewSlice, ReviewResponse::from);
                })
                .orElseGet(() -> {
                    Slice<Review> emptySlice = new SliceImpl<>(Collections.emptyList(), adjustedPageable, false);
                    return SliceResponse.from(emptySlice, ReviewResponse::from);
                });
    }

    @Override
    @Transactional
    public void updateReview(Long reviewId, UpdateReviewRequest request, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Review review = domainFinder.findReviewAndVerifyOwner(reviewId, user.getId());

        review.update(request.content(), request.rating());
        eventPublisher.publishEvent(new ReviewChangedEvent(review.getMedia().getId()));

        log.info("리뷰가 수정되었습니다. 리뷰 ID: {}", reviewId);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Review review = domainFinder.findReviewAndVerifyOwner(reviewId, user.getId());
        Long mediaId = review.getMedia().getId();

        reviewRepository.delete(review);
        eventPublisher.publishEvent(new ReviewChangedEvent(mediaId));

        log.info("리뷰가 삭제되었습니다. 리뷰 ID: {}", reviewId);
    }

    @Override
    public boolean isAuthor(Long reviewId, String username) {
        return reviewRepository.findById(reviewId)
                .map(review -> review.getUser().getEmail().equals(username))
                .orElse(false);
    }

    private Pageable adjustPageable(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > reviewProperties.getMaxPageSize()) {
            size = reviewProperties.getDefaultPageSize();
        }

        return PageRequest.of(page, size, pageable.getSort());
    }
}