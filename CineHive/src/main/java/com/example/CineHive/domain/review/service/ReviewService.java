package com.example.CineHive.domain.review.service;

import com.example.CineHive.global.dto.SliceResponse;
import com.example.CineHive.domain.media.MediaType;
import com.example.CineHive.domain.review.dto.CreateReviewRequest;
import com.example.CineHive.domain.review.dto.ReviewResponse;
import com.example.CineHive.domain.review.dto.UpdateReviewRequest;
import org.springframework.data.domain.Pageable;

/**
 * 리뷰 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface ReviewService {

    /**
     * 특정 미디어에 대한 새로운 리뷰를 생성합니다.
     *
     * @param tmdbId    리뷰를 작성할 미디어의 TMDB ID
     * @param mediaType 리뷰를 작성할 미디어의 타입 (MOVIE 또는 TV)
     * @param request   리뷰 생성에 필요한 정보(내용, 별점) DTO
     * @param userEmail 리뷰를 작성하는 사용자의 이메일 (인증 정보)
     * @return 생성된 리뷰의 상세 정보 DTO
     */
    ReviewResponse createReview(Integer tmdbId, MediaType mediaType, CreateReviewRequest request, String userEmail);

    /**
     * 특정 미디어의 리뷰 목록을 페이징하여 조회합니다.
     *
     * @param tmdbId    리뷰 목록을 조회할 미디어의 TMDB ID
     * @param mediaType 미디어 타입 (MOVIE 또는 TV)
     * @param pageable  페이징 및 정렬 정보
     * @return 슬라이싱된 리뷰 목록 응답 DTO
     */
    SliceResponse<ReviewResponse> getReviewsForMedia(Integer tmdbId, MediaType mediaType, Pageable pageable);

    /**
     * 특정 리뷰를 수정합니다.
     *
     * @param reviewId  수정할 리뷰의 고유 ID
     * @param request   리뷰 수정 정보(내용, 별점) DTO
     * @param userEmail 수정을 시도하는 사용자의 이메일 (인증 정보)
     */
    void updateReview(Long reviewId, UpdateReviewRequest request, String userEmail);

    /**
     * 특정 리뷰를 삭제합니다.
     *
     * @param reviewId  삭제할 리뷰의 고유 ID
     * @param userEmail 삭제를 시도하는 사용자의 이메일 (인증 정보)
     */
    void deleteReview(Long reviewId, String userEmail);

    /**
     * 특정 리뷰가 해당 사용자에 의해 작성되었는지 확인합니다. (Spring Security 권한 검증용)
     *
     * @param reviewId 검증할 리뷰의 고유 ID
     * @param username 검증할 사용자의 이메일 (principal.username)
     * @return 소유자가 맞으면 true, 아니면 false
     */
    boolean isAuthor(Long reviewId, String username);
}