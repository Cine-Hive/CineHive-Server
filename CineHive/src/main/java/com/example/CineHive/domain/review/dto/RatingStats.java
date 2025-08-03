package com.example.CineHive.domain.review.dto;

/**
 * 리뷰의 평균 별점과 전체 개수를 한 번의 쿼리로 조회하기 위한 DTO입니다.
 * @param average calculated average rating
 * @param count total number of reviews
 */
public record RatingStats(double average, long count) {}
