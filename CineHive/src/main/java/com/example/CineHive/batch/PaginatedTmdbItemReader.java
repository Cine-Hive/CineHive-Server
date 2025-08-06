package com.example.CineHive.batch;

import com.example.CineHive.client.tmdb.dto.TmdbPagedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * TMDB의 페이징된 API 응답을 마지막 페이지까지 순차적으로 읽어오는 제네릭 ItemReader입니다.
 * @param <T> API 응답 결과 리스트의 아이템 타입
 */
@Slf4j
public class PaginatedTmdbItemReader<T> implements ItemReader<T> {

    private final Function<Integer, TmdbPagedResponse<T>> pageLoader;
    private int currentPage = 1;
    private int totalPages = 1; // API 응답을 받은 후 실제 값으로 설정됨
    private Iterator<T> buffer = Collections.emptyIterator();

    public PaginatedTmdbItemReader(Function<Integer, TmdbPagedResponse<T>> pageLoader) {
        this.pageLoader = pageLoader;
    }

    @Override
    public T read() throws Exception {
        // 현재 버퍼에 읽을 아이템이 없으면 다음 페이지를 로드
        if (!buffer.hasNext()) {
            if (currentPage > totalPages) {
                log.info("모든 페이지 처리를 완료했습니다.");
                return null; // 모든 페이지를 다 읽었으면 null을 반환하여 종료
            }

            log.info("TMDB API에서 {} 페이지를 로드합니다.", currentPage);
            TmdbPagedResponse<T> response = pageLoader.apply(currentPage++);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("API 응답이 비어있어 읽기를 중단합니다.");
                return null; // 응답이 비었으면 종료
            }

            this.totalPages = response.getTotalPages();
            List<T> results = response.getResults();
            this.buffer = results.iterator();
        }

        return buffer.next();
    }
}