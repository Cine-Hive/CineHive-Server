package com.example.CineHive.domain.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import com.example.CineHive.domain.media.enums.MediaType;
import com.example.CineHive.domain.search.document.*;
import com.example.CineHive.domain.search.dto.*;
import com.example.CineHive.global.dto.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final StringRedisTemplate redisTemplate;
    private static final String TRENDING_SEARCHES_KEY = "trending_searches";

    @Override
    @Async("taskExecutor")
    public CompletableFuture<SearchAllResponse> searchAll(String query) {
        log.debug("통합 검색을 시작합니다. Query: {}", query);
        logSearchKeyword(query);

        Pageable pageable = PageRequest.of(0, 5);

        // 비동기로 각 검색 실행
        CompletableFuture<List<MediaSearchResponse>> mediaFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return searchMedia(query, null, pageable).content();
            } catch (Exception e) {
                log.error("미디어 검색 중 오류 발생", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<PostSearchResponse>> postsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return searchPosts(query, pageable).content();
            } catch (Exception e) {
                log.error("게시글 검색 중 오류 발생", e);
                return Collections.emptyList();
            }
        });

        CompletableFuture<List<PersonSearchResponse>> peopleFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return searchPeople(query, pageable).content();
            } catch (Exception e) {
                log.error("인물 검색 중 오류 발생", e);
                return Collections.emptyList();
            }
        });

        return CompletableFuture.allOf(mediaFuture, postsFuture, peopleFuture)
                .thenApply(v -> SearchAllResponse.builder()
                        .media(mediaFuture.join())
                        .posts(postsFuture.join())
                        .people(peopleFuture.join())
                        .build());
    }

    @Override
    public SliceResponse<MediaSearchResponse> searchMedia(String query, MediaType mediaType, Pageable pageable) {
        if (!elasticsearchOperations.indexOps(MediaDocument.class).exists()) {
            log.warn("인덱스 'media'가 존재하지 않으므로 빈 결과를 반환합니다.");
            return SliceResponse.empty(pageable);
        }

        log.debug("Elasticsearch 미디어 검색을 시작합니다. Query: {}, Type: {}", query, mediaType);
        if (pageable.isPaged() && pageable.getPageNumber() == 0) {
            logSearchKeyword(query);
        }

        try {
            // 더 정확한 검색을 위해 bool query 구성
            Query multiMatchQuery = Query.of(q -> q
                    .multiMatch(mm -> mm
                            .query(query)
                            .fields("title^3", "title.search_as_you_type^2", "overview", "cast^2")
                            .type(TextQueryType.BestFields)
                            .fuzziness("AUTO")
                            .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.Or)
                    ));

            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder()
                    .must(multiMatchQuery);

            // 미디어 타입 필터링
            if (mediaType != null) {
                // MediaType enum의 실제 값 확인 필요 - getValue() 또는 name() 중 하나 사용
                String mediaTypeValue = mediaType.name(); // 또는 mediaType.getValue()
                boolQueryBuilder.filter(f -> f
                        .term(t -> t
                                .field("mediaType")
                                .value(mediaTypeValue)));
            }

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(boolQueryBuilder.build()))
                    .withPageable(pageable)
                    .build();

            SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(nativeQuery, MediaDocument.class);
            SearchPage<MediaDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

            log.debug("미디어 검색 결과: {}개 발견", searchHits.getTotalHits());
            return SliceResponse.from(resultPage.map(SearchHit::getContent), MediaSearchResponse::from);
        } catch (Exception e) {
            log.error("미디어 검색 중 오류 발생: {}", e.getMessage(), e);
            return SliceResponse.empty(pageable);
        }
    }

    @Override
    public SliceResponse<PostSearchResponse> searchPosts(String query, Pageable pageable) {
        if (!elasticsearchOperations.indexOps(PostDocument.class).exists()) {
            log.warn("인덱스 'posts'가 존재하지 않으므로 빈 결과를 반환합니다.");
            return SliceResponse.empty(pageable);
        }

        log.debug("Elasticsearch 게시글 검색을 시작합니다. Query: {}", query);
        if (pageable.isPaged() && pageable.getPageNumber() == 0) {
            logSearchKeyword(query);
        }

        try {
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .multiMatch(mm -> mm
                                    .query(query)
                                    .fields("title^2", "content")
                                    .type(TextQueryType.BestFields)
                                    .fuzziness("AUTO")))
                    .withPageable(pageable)
                    .build();

            SearchHits<PostDocument> searchHits = elasticsearchOperations.search(nativeQuery, PostDocument.class);
            SearchPage<PostDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

            log.debug("게시글 검색 결과: {}개 발견", searchHits.getTotalHits());
            return SliceResponse.from(resultPage.map(SearchHit::getContent), PostSearchResponse::from);
        } catch (Exception e) {
            log.error("게시글 검색 중 오류 발생: {}", e.getMessage(), e);
            return SliceResponse.empty(pageable);
        }
    }

    @Override
    public SliceResponse<PersonSearchResponse> searchPeople(String query, Pageable pageable) {
        if (!elasticsearchOperations.indexOps(PersonDocument.class).exists()) {
            log.warn("인덱스 'persons'가 존재하지 않으므로 빈 결과를 반환합니다.");
            return SliceResponse.empty(pageable);
        }

        log.debug("Elasticsearch 인물 검색을 시작합니다. Query: {}", query);
        if (pageable.isPaged() && pageable.getPageNumber() == 0) {
            logSearchKeyword(query);
        }

        try {
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .match(m -> m
                                    .field("name")
                                    .query(query)
                                    .fuzziness("AUTO")))
                    .withPageable(pageable)
                    .build();

            SearchHits<PersonDocument> searchHits = elasticsearchOperations.search(nativeQuery, PersonDocument.class);
            SearchPage<PersonDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

            log.debug("인물 검색 결과: {}개 발견", searchHits.getTotalHits());
            return SliceResponse.from(resultPage.map(SearchHit::getContent), PersonSearchResponse::from);
        } catch (Exception e) {
            log.error("인물 검색 중 오류 발생: {}", e.getMessage(), e);
            return SliceResponse.empty(pageable);
        }
    }

    @Override
    public SliceResponse<UserSearchResponse> searchUsers(String nickname, Pageable pageable) {
        if (!elasticsearchOperations.indexOps(UserDocument.class).exists()) {
            log.warn("인덱스 'users'가 존재하지 않으므로 빈 결과를 반환합니다.");
            return SliceResponse.empty(pageable);
        }

        log.debug("Elasticsearch 사용자 검색을 시작합니다. Nickname: {}", nickname);
        logSearchKeyword(nickname);

        try {
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .match(m -> m
                                    .field("nickname")
                                    .query(nickname)
                                    .fuzziness("AUTO")))
                    .withPageable(pageable)
                    .build();

            SearchHits<UserDocument> searchHits = elasticsearchOperations.search(nativeQuery, UserDocument.class);
            SearchPage<UserDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

            return SliceResponse.from(resultPage.map(SearchHit::getContent),
                    userDoc -> new UserSearchResponse(userDoc.id(), userDoc.nickname(), userDoc.profileImageUrl()));
        } catch (Exception e) {
            log.error("사용자 검색 중 오류 발생: {}", e.getMessage(), e);
            return SliceResponse.empty(pageable);
        }
    }

    @Override
    public SliceResponse<CollectionSearchResponse> searchCollections(String query, Pageable pageable) {
        if (!elasticsearchOperations.indexOps(CollectionDocument.class).exists()) {
            log.warn("인덱스 'collections'가 존재하지 않으므로 빈 결과를 반환합니다.");
            return SliceResponse.empty(pageable);
        }

        log.debug("Elasticsearch 컬렉션 검색을 시작합니다. Query: {}", query);
        logSearchKeyword(query);

        try {
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .match(m -> m
                                    .field("name")
                                    .query(query)
                                    .fuzziness("AUTO")))
                    .withPageable(pageable)
                    .build();

            SearchHits<CollectionDocument> searchHits = elasticsearchOperations.search(nativeQuery, CollectionDocument.class);
            SearchPage<CollectionDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

            return SliceResponse.from(resultPage.map(SearchHit::getContent),
                    doc -> new CollectionSearchResponse(doc.id(), doc.name(), doc.posterPath()));
        } catch (Exception e) {
            log.error("컬렉션 검색 중 오류 발생: {}", e.getMessage(), e);
            return SliceResponse.empty(pageable);
        }
    }

    @Override
    public SliceResponse<MediaSearchResponse> searchMediaByKeyword(String keyword, Pageable pageable) {
        if (!elasticsearchOperations.indexOps(MediaDocument.class).exists()) {
            log.warn("인덱스 'media'가 존재하지 않으므로 빈 결과를 반환합니다.");
            return SliceResponse.empty(pageable);
        }

        log.debug("Elasticsearch 키워드 기반 미디어 검색을 시작합니다. Keyword: {}", keyword);
        logSearchKeyword(keyword);

        try {
            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .term(t -> t
                                    .field("keywords")
                                    .value(keyword)))
                    .withPageable(pageable)
                    .build();

            SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(nativeQuery, MediaDocument.class);
            SearchPage<MediaDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

            return SliceResponse.from(resultPage.map(SearchHit::getContent), MediaSearchResponse::from);
        } catch (Exception e) {
            log.error("키워드 기반 미디어 검색 중 오류 발생: {}", e.getMessage(), e);
            return SliceResponse.empty(pageable);
        }
    }

    @Override
    public List<String> getSearchSuggestions(String prefix) {
        if (!elasticsearchOperations.indexOps(MediaDocument.class).exists()) {
            return Collections.emptyList();
        }

        try {
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(q -> q
                            .multiMatch(mm -> mm
                                    .query(prefix)
                                    .type(TextQueryType.BoolPrefix)
                                    .fields("title.search_as_you_type", "title.search_as_you_type._2gram", "title.search_as_you_type._3gram")))
                    .withMaxResults(10)
                    .build();

            SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(searchQuery, MediaDocument.class);
            return searchHits.getSearchHits().stream()
                    .map(hit -> hit.getContent().getTitle())
                    .distinct()
                    .toList();
        } catch (Exception e) {
            log.error("검색 제안 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getAutocompletions(String prefix) {
        if (!elasticsearchOperations.indexOps(MediaDocument.class).exists()) {
            return Collections.emptyList();
        }

        try {
            NativeQuery searchQuery = NativeQuery.builder()
                    .withSuggester(Suggester.of(s -> s
                            .suggesters("title-suggester", fs -> fs
                                    .prefix(prefix)
                                    .completion(cs -> cs
                                            .field("title_suggest")
                                            .size(10)))))
                    .build();

            SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(searchQuery, MediaDocument.class);

            if (searchHits.getSuggest() != null) {
                return searchHits.getSuggest()
                        .getSuggestion("title-suggester")
                        .getEntries().stream()
                        .flatMap(entry -> entry.getOptions().stream())
                        .map(Suggest.Suggestion.Entry.Option::getText)
                        .toList();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("자동완성 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TrendingSearchResponse> getTrendingSearches() {
        try {
            ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
            Set<String> trendingKeywords = zSetOperations.reverseRange(TRENDING_SEARCHES_KEY, 0, 9);

            if (trendingKeywords == null || trendingKeywords.isEmpty()) {
                return Collections.emptyList();
            }

            AtomicInteger rank = new AtomicInteger(1);
            return trendingKeywords.stream()
                    .map(keyword -> new TrendingSearchResponse(rank.getAndIncrement(), keyword))
                    .toList();
        } catch (Exception e) {
            log.error("인기 검색어 조회 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private void logSearchKeyword(String query) {
        if (query != null && !query.isBlank()) {
            try {
                redisTemplate.opsForZSet().incrementScore(TRENDING_SEARCHES_KEY, query.trim(), 1);
            } catch (Exception e) {
                log.error("Redis에 검색어 로깅 중 오류 발생. Keyword: {}", query, e);
            }
        }
    }
}