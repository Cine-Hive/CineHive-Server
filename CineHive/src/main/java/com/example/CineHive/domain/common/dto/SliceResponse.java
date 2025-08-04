package com.example.CineHive.domain.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Slice;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Slice 형태의 데이터를 API 응답으로 변환하기 위한 제네릭 record 입니다.
 * 불변 객체이며, '더보기', '무한 스크롤' UI에 최적화된 메타데이터를 포함합니다.
 */
@Schema(description = "슬라이스 기반 페이징 응답 DTO")
public record SliceResponse<T>(
        @Schema(description = "데이터 목록")
        List<T> content,
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int currentPage,
        @Schema(description = "페이지 크기", example = "10")
        int pageSize,
        @Schema(description = "첫 번째 페이지 여부", example = "true")
        boolean isFirst,
        @Schema(description = "마지막 페이지 여부", example = "false")
        boolean isLast,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    /**
     * Slice 객체와 DTO 변환 함수를 받아 SliceResponse를 생성하는 정적 팩토리 메서드입니다.
     * @param slice     원본 Slice 객체 (null일 경우 안전하게 처리)
     * @param converter Entity를 DTO로 변환하는 함수
     * @return SliceResponse 객체
     * @param <E> Entity 타입
     * @param <T> DTO 타입
     */
    public static <E, T> SliceResponse<T> from(Slice<E> slice, Function<E, T> converter) {
        // ✔ Null-Safety 강화
        if (slice == null || !slice.hasContent()) {
            return new SliceResponse<>(Collections.emptyList(), 0, 0, true, true, false);
        }

        List<T> content = slice.getContent().stream()
                .map(converter)
                .toList(); // Java 16+ .collect(Collectors.toList())와 동일

        return new SliceResponse<>(
                content,
                slice.getNumber(),
                slice.getSize(),
                slice.isFirst(),
                slice.isLast(),
                slice.hasNext()
        );
    }
}