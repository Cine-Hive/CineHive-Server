package com.example.CineHive.domain.person.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.domain.person.dto.FilmographyResponse;
import com.example.CineHive.domain.person.dto.PersonDetailsResponse;
import com.example.CineHive.domain.person.dto.PersonInListResponse;
import com.example.CineHive.global.dto.SliceResponse;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.exception.TmdbClientException;
import com.example.CineHive.global.properties.TmdbProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "persons")
public class PersonQueryServiceImpl implements PersonQueryService {

    private final TmdbApiClient tmdbApiClient;
    private final TmdbProperties tmdbProperties;

    // TODO: @CacheConfig 또는 yml에서 TTL 등 구체적인 캐시 정책 설정 필요
    @Override
    @Cacheable(value = "personDetails", key = "'person:' + #personTmdbId")
    public PersonDetailsResponse getPersonDetails(Long personTmdbId) {
        log.debug("인물 상세 정보를 조회합니다. (TMDB ID: {})", personTmdbId);
        throw new BusinessException(ErrorCode.FEATURE_NOT_IMPLEMENTED);
    }

    @Override
    @Cacheable(cacheNames = "popularPeople",
            key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public SliceResponse<PersonInListResponse> getPopularPeople(Pageable pageable) {
        int requestedOffset = (int) pageable.getOffset();
        int requestedSize = pageable.getPageSize();

        int tmdbPageSize = tmdbProperties.getPageSize();
        int startTmdbPage = (requestedOffset / tmdbPageSize) + 1;
        int endTmdbPage = ((requestedOffset + requestedSize - 1) / tmdbPageSize) + 1;

        // 6. 로그 메시지 상세화
        log.debug("인기 인물 목록 조회. Pageable: {}, TMDB Page Range: {} ~ {}", pageable, startTmdbPage, endTmdbPage);

        List<TmdbPersonInListResponse> tmdbResults = new ArrayList<>();
        TmdbPagedResponse<TmdbPersonInListResponse> lastTmdbResponse = null;

        try {
            for (int i = startTmdbPage; i <= endTmdbPage; i++) {
                lastTmdbResponse = tmdbApiClient.getPopularPeople(i);
                if (lastTmdbResponse != null && lastTmdbResponse.getResults() != null) {
                    tmdbResults.addAll(lastTmdbResponse.getResults());
                }
            }
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.TMDB_API_ERROR, e);
        }

        int startOffsetInTmdbResults = requestedOffset % tmdbPageSize;

        List<PersonInListResponse> content = tmdbResults.stream()
                .skip(startOffsetInTmdbResults)
                .limit(requestedSize)
                .map(PersonInListResponse::from)
                .toList();

        boolean hasNext = false;
        if (lastTmdbResponse != null) {
            boolean hasMoreInCurrentFetch = tmdbResults.size() > startOffsetInTmdbResults + requestedSize;
            boolean hasMoreOnTmdbServer = lastTmdbResponse.getPage() < lastTmdbResponse.getTotalPages();
            hasNext = hasMoreInCurrentFetch || hasMoreOnTmdbServer;
        }

        Slice<PersonInListResponse> slice = new SliceImpl<>(content, pageable, hasNext);
        return SliceResponse.from(slice, Function.identity());
    }

    @Override
    @Cacheable(cacheNames = "filmographies",
            key = "'person:' + #personTmdbId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public SliceResponse<FilmographyResponse> getFilmography(Long personTmdbId, Pageable pageable) {
        log.debug("인물 필모그래피를 조회합니다. TMDB ID: {}, Pageable: {}", personTmdbId, pageable);

        try {
            TmdbPersonDetailResponse personDetail = tmdbApiClient.getPersonDetail(personTmdbId);

            List<TmdbPersonCreditResponse> allCredits = Stream.of(personDetail.movieCredits(), personDetail.tvCredits())
                    .filter(Objects::nonNull)
                    .flatMap(credits -> Stream.concat(
                            Optional.ofNullable(credits.cast()).stream().flatMap(List::stream),
                            Optional.ofNullable(credits.crew()).stream().flatMap(List::stream)
                    )).toList();

            List<FilmographyResponse> sortedFilmography = allCredits.stream()
                    .filter(credit -> credit.id() != null && credit.getReleaseDate() != null)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(TmdbPersonCreditResponse::id, Function.identity(), (e1, e2) -> e1, LinkedHashMap::new),
                            map -> new ArrayList<>(map.values())
                    )).stream()
                    .sorted(Comparator.comparing(TmdbPersonCreditResponse::getReleaseDate).reversed())
                    .map(FilmographyResponse::from)
                    .toList();

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), sortedFilmography.size());

            List<FilmographyResponse> content = (start >= sortedFilmography.size())
                    ? Collections.emptyList()
                    : sortedFilmography.subList(start, end);

            boolean hasNext = pageable.getPageSize() > 0 && end < sortedFilmography.size();
            Slice<FilmographyResponse> slice = new SliceImpl<>(content, pageable, hasNext);

            return SliceResponse.from(slice, Function.identity());

        } catch (TmdbClientException e) {
            if (e.getHttpStatus() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(ErrorCode.PERSON_NOT_FOUND, e);
            }
            throw new BusinessException(ErrorCode.TMDB_API_ERROR, e);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.TMDB_API_ERROR, e);
        }
    }

    /**
     * 출연(cast) 및 제작(crew) 정보를 하나의 리스트에 추가하는 헬퍼 메서드
     */
    private void addCreditsToList(List<TmdbPersonCreditResponse> allCredits, TmdbPersonCreditsResponse credits) {
        if (credits != null) {
            if (credits.cast() != null) allCredits.addAll(credits.cast());
            if (credits.crew() != null) allCredits.addAll(credits.crew());
        }
    }
}