package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * TMDB API의 페이징 처리된 목록 응답을 위한 제네릭 DTO입니다.
 * Jackson의 안정적인 제네릭 역직렬화를 위해 record 대신 일반 클래스로 정의합니다.
 * @param <T> 목록에 포함될 콘텐츠의 타입
 */
@Getter
@Setter
@NoArgsConstructor
public class TmdbPagedResponse<T> {
        private int page;
        private List<T> results;
        @JsonProperty("total_pages")
        private int totalPages;
        @JsonProperty("total_results")
        private int totalResults;
}