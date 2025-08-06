package com.example.CineHive.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

/**
 * Spring Batch Step에서 아이템이 스킵될 때 로그를 남기는 리스너입니다.
 * @param <T> 아이템 타입
 */
@Slf4j
public class LoggingSkipListener<T> implements SkipListener<T, Object> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Reader 단계에서 아이템 스킵 발생. 원인: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(T item, Throwable t) {
        log.warn("Processor 단계에서 아이템 {} 스킵 발생. 원인: {}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("Writer 단계에서 아이템 {} 스킵 발생. 원인: {}", item, t.getMessage());
    }
}