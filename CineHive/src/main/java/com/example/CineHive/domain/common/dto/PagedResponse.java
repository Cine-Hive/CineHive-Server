package com.example.CineHive.domain.common.dto;

import com.example.CineHive.client.tmdb.dto.TmdbPagedResponse;
import com.example.CineHive.domain.media.dto.MediaChartResponse;
import com.example.CineHive.domain.media.dto.MediaSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Schema(description = "페이징 처리된 목록 응답")
public record PagedResponse<T>(
        @Schema(description = "현재 페이지의 콘텐츠 목록")
        List<T> content,
        @Schema(description = "현재 페이지 번호 (1부터 시작)")
        int page,
        @Schema(description = "페이지 당 항목 수")
        int size,
        @Schema(description = "전체 항목 수")
        long totalElements,
        @Schema(description = "전체 페이지 수")
        int totalPages,
        @Schema(description = "마지막 페이지 여부")
        boolean last
) {
    public static <T> PagedResponse<T> empty(int page, int size) {
        return new PagedResponse<>(
                Collections.emptyList(),
                page,
                size,
                0L,
                0,
                true
        );
    }

    public static <T, R> PagedResponse<R> from(TmdbPagedResponse<T> tmdbResponse, Function<T, R> mapper) {
        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PagedResponse.empty(1, 20);
        }

        List<R> content = tmdbResponse.getResults().stream()
                .map(mapper)
                .toList();

        return new PagedResponse<>(
                content,
                tmdbResponse.getPage(),
                content.size(),
                (long) tmdbResponse.getTotalResults(),
                tmdbResponse.getTotalPages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotalPages()
        );
    }

    public static <T> PagedResponse<MediaChartResponse> fromChart(
            TmdbPagedResponse<T> tmdbResponse, Function<T, MediaSummaryResponse> mapper) {

        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PagedResponse.empty(1, 20);
        }

        AtomicInteger ranker = new AtomicInteger(
                (tmdbResponse.getPage() - 1) * 20
        );

        List<MediaChartResponse> content = tmdbResponse.getResults().stream()
                .map(item -> {
                    MediaSummaryResponse summary = mapper.apply(item);
                    return MediaChartResponse.from(summary, ranker.incrementAndGet());
                })
                .toList();

        return new PagedResponse<>(
                content,
                tmdbResponse.getPage(),
                content.size(),
                (long) tmdbResponse.getTotalResults(),
                tmdbResponse.getTotalPages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotalPages()
        );
    }
}
