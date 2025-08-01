package com.example.CineHive.domain.review;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.domain.common.DomainFinder;
import com.example.CineHive.domain.common.dto.PagedResponse;
import com.example.CineHive.domain.media.Media;
import com.example.CineHive.domain.media.MediaRepository;
import com.example.CineHive.domain.media.MediaType;
import com.example.CineHive.domain.review.dto.CreateReviewRequest;
import com.example.CineHive.domain.review.dto.ReviewResponse;
import com.example.CineHive.domain.review.dto.UpdateReviewRequest;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MediaRepository mediaRepository;
    private final DomainFinder domainFinder;
    private final TmdbApiClient tmdbApiClient;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Media media = findOrCreateMedia(request.tmdbId(), request.mediaType());

        if (reviewRepository.existsByUserAndMedia(user, media)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        double ratingValue = Double.parseDouble(request.rating());
        Review review = Review.builder()
                .user(user)
                .media(media)
                .content(request.content())
                .rating(ratingValue)
                .build();

        Review savedReview = reviewRepository.save(review);
        updateMediaRating(media);

        log.info("새로운 리뷰가 생성되었습니다. 리뷰 ID: {}, 미디어 ID: {}", savedReview.getId(), media.getId());
        return ReviewResponse.of(savedReview);
    }

    @Override
    public PagedResponse<ReviewResponse> getReviewsForMedia(Integer tmdbId, MediaType mediaType, Pageable pageable) {
        return mediaRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                .map(media -> {
                    Page<Review> reviewPage = reviewRepository.findByMedia(media, pageable);
                    return PagedResponse.from(reviewPage, ReviewResponse::of);
                })
                .orElseGet(PagedResponse::empty);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Review review = findReviewAndVerifyOwner(reviewId, user.getId());

        double newRating = Double.parseDouble(request.rating());
        review.update(request.content(), newRating);
        updateMediaRating(review.getMedia());

        log.info("리뷰가 수정되었습니다. 리뷰 ID: {}", reviewId);
        return ReviewResponse.of(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Review review = findReviewAndVerifyOwner(reviewId, user.getId());
        Media media = review.getMedia();

        reviewRepository.delete(review);
        updateMediaRating(media);

        log.info("리뷰가 삭제되었습니다. 리뷰 ID: {}", reviewId);
    }

    private Media findOrCreateMedia(Integer tmdbId, MediaType mediaType) {
        return mediaRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                .orElseGet(() -> {
                    log.info("DB에 미디어 정보가 없어 TMDB API를 통해 새로 생성합니다. TMDB ID: {}, 타입: {}", tmdbId, mediaType);
                    Media newMedia = createMediaFromTmdb(tmdbId, mediaType);
                    return mediaRepository.save(newMedia);
                });
    }

    private Media createMediaFromTmdb(Integer tmdbId, MediaType mediaType) {
        if (mediaType.isMovie()) {
            var tmdb = tmdbApiClient.getMovieDetail(tmdbId.longValue());
            return Media.builder()
                    .tmdbId(tmdb.id().intValue())
                    .mediaType(MediaType.MOVIE)
                    .title(tmdb.title())
                    .posterPath(tmdb.posterPath())
                    .releaseDate(tmdb.releaseDate())
                    .build();
        } else {
            var tmdb = tmdbApiClient.getTvSeriesDetail(tmdbId.longValue());
            return Media.builder()
                    .tmdbId(tmdb.id().intValue())
                    .mediaType(MediaType.TV)
                    .title(tmdb.name())
                    .posterPath(tmdb.posterPath())
                    .releaseDate(tmdb.firstAirDate())
                    .build();
        }
    }

    private void updateMediaRating(Media media) {
        double totalRating = reviewRepository.sumRatingByMedia(media).orElse(0.0);
        int reviewCount = (int) reviewRepository.countByMedia(media);
        media.updateRating(totalRating, reviewCount);
    }

    private Review findReviewAndVerifyOwner(Long reviewId, Long userId) {
        Review review = domainFinder.findReviewById(reviewId);
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return review;
    }
}