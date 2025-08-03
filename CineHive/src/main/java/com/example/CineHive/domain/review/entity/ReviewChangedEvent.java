package com.example.CineHive.domain.review.entity;

import lombok.Getter;

/**
 * 리뷰 생성, 수정, 삭제 시 발생하는 이벤트를 정의하는 클래스입니다.
 * 이 이벤트는 미디어의 평균 평점 및 리뷰 수를 비동기적으로 업데이트하는 데 사용됩니다.
 */
@Getter
public class ReviewChangedEvent {

    private final Long mediaId;

    public ReviewChangedEvent(Long mediaId) {
        this.mediaId = mediaId;
    }
}