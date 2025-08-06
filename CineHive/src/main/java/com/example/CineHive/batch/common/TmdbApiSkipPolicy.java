package com.example.CineHive.batch.common;

import com.example.CineHive.global.exception.TmdbClientException;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.http.HttpStatus;

/**
 * TMDB API 예외에 대한 커스텀 Skip 정책입니다.
 */
public class TmdbApiSkipPolicy implements SkipPolicy {
    @Override
    public boolean shouldSkip(Throwable t, long stepExecutionId) throws SkipLimitExceededException { // int skipCount -> long stepExecutionId
        if (t instanceof TmdbClientException ex) {
            return ex.getHttpStatus() == HttpStatus.NOT_FOUND;
        }
        return false;
    }
}