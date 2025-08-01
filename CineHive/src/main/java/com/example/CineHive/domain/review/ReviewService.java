package com.example.CineHive.domain.review;

import com.example.CineHive.domain.common.dto.PagedResponse;
import com.example.CineHive.domain.review.dto.CreateReviewRequest;
import com.example.CineHive.domain.review.dto.ReviewResponse;
import com.example.CineHive.domain.review.dto.UpdateReviewRequest;
import com.example.CineHive.domain.media.MediaType;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /**
     * 특정 미디어에 대한 새로운 리뷰를 생성합니다.
     * @param request 리뷰 생성 정보 DTO
     * @param userEmail 리뷰를 작성하는 사용자 이메일
     * @return 생성된 리뷰 정보 DTO
     */
    ReviewResponse createReview(CreateReviewRequest request, String userEmail);

    /**
     * 특정 미디어의 리뷰 목록을 페이징하여 조회합니다.
     * @param tmdbId 미디어의 TMDB ID
     * @param mediaType 미디어 타입
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 리뷰 목록 응답 DTO
     */
    PagedResponse<ReviewResponse> getReviewsForMedia(Integer tmdbId, MediaType mediaType, Pageable pageable);

    /**
     * 특정 리뷰를 수정합니다.
     * @param reviewId 수정할 리뷰 ID
     * @param request 리뷰 수정 정보 DTO
     * @param userEmail 수정을 시도하는 사용자 이메일
     * @return 수정된 리뷰 정보 DTO
     */
    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, String userEmail);

    /**
     * 특정 리뷰를 삭제합니다.
     * @param reviewId 삭제할 리뷰 ID
     * @param userEmail 삭제를 시도하는 사용자 이메일
     */
    void deleteReview(Long reviewId, String userEmail);
}