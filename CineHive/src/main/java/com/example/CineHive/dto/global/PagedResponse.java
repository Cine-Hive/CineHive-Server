package com.example.CineHive.dto.global;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;

@Schema(description = "페이징 처리된 목록 응답")
public record PagedResponse<T>(
        @Schema(description = "현재 페이지의 콘텐츠 목록")
        List<T> content,
        @Schema(description = "현재 페이지 번호 (0부터 시작)")
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
    /**
     * 비어 있는 PagedResponse 객체를 생성합니다.
     * @param page 요청받은 페이지 번호
     * @param size 요청받은 페이지 크기
     * @return 내용물이 비어 있는 PagedResponse 객체
     */
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
}