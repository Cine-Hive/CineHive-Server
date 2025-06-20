package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /**
     * 비어있는 페이지 응답을 생성하는 정적 팩토리 메서드
     * @param page 요청된 페이지 번호
     * @param size 요청된 페이지 크기
     * @return 비어있는 PagedResponse 객체
     */
    public static <T> PagedResponse<T> empty(int page, int size) {
        return PagedResponse.<T>builder()
                .content(Collections.emptyList())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();
    }
}