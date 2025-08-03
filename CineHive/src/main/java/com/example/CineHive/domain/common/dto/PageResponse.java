package com.example.CineHive.domain.common.controller.dto;

import com.example.CineHive.client.tmdb.dto.TmdbPagedResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Schema(description = "페이징 처리된 목록 응답")
public record PageResponse<T>(
        @Schema(description = "현재 페이지의 콘텐츠 목록")
        List<T> content,
        @Schema(description = "현재 페이지 번호 (1부터 시작)")
        int pageNumber,
        @Schema(description = "페이지 당 항목 수")
        int pageSize,
        @Schema(description = "전체 항목 수")
        long totalElements,
        @Schema(description = "전체 페이지 수")
        int totalPages,
        @Schema(description = "마지막 페이지 여부")
        boolean isLast
) {
    /**
     * Spring Data JPA의 Page 객체를 PagedResponse DTO로 변환합니다.
     */
    public static <E, D> PageResponse<D> from(Page<E> page, Function<E, D> mapper) {
        List<D> content = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(
                content,
                page.getNumber() + 1, // Page는 0-based, API는 1-based
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * TMDB API 응답을 PagedResponse DTO로 변환합니다.
     */
    public static <E, D> PageResponse<D> from(TmdbPagedResponse<E> tmdbResponse, Function<E, D> mapper) {
        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return empty();
        }
        List<D> content = tmdbResponse.getResults().stream().map(mapper).toList();
        return new PageResponse<>(
                content,
                tmdbResponse.getPage(),
                content.size(),
                (long) tmdbResponse.getTotalResults(),
                tmdbResponse.getTotalPages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotalPages()
        );
    }

    /**
     * 비어있는 PagedResponse 객체를 생성합니다.
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(Collections.emptyList(), 1, 0, 0L, 0, true);
    }
}