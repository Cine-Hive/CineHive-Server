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
    private int size; // <-- size 필드 추가!
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T> PagedResponse<T> empty(int page, int size) {
        return PagedResponse.<T>builder()
                .content(Collections.emptyList())
                .page(page)
                .size(size) // <-- size 필드 설정 추가
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();
    }
}