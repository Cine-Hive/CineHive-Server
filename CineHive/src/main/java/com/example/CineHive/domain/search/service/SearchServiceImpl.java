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
import org.springframework.data.domain.Page;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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
        CompletableFuture<List<MediaSearchResponse>> mediaFuture = CompletableFuture.supplyAsync(() ->
                searchMedia(query, null, pageable).content());
        CompletableFuture<List<PostSearchResponse>> postsFuture = CompletableFuture.supplyAsync(() ->
                searchPosts(query, pageable).content());
        CompletableFuture<List<PersonSearchResponse>> peopleFuture = CompletableFuture.supplyAsync(() ->
                searchPeople(query, pageable).content());

        return CompletableFuture.allOf(mediaFuture, postsFuture, peopleFuture)
                .thenApply(v -> SearchAllResponse.builder()
                        .media(mediaFuture.join())
                        .posts(postsFuture.join())
                        .people(peopleFuture.join())
                        .build());
    }

    @Override
    public SliceResponse<MediaSearchResponse> searchMedia(String query, MediaType mediaType, Pageable pageable) {
        log.debug("Elasticsearch 미디어 검색을 시작합니다. Query: {}, Type: {}", query, mediaType);
        if (pageable.isPaged() && pageable.getPageNumber() == 0) {
            logSearchKeyword(query);
        }

        Query multiMatchQuery = Query.of(q -> q
                .multiMatch(mm -> mm
                        .query(query)
                        .fields("title^3", "overview")
                        .type(TextQueryType.BestFields)
                )
        );

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder().must(multiMatchQuery);
        if (mediaType != null) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("mediaType").value(mediaType.getValue())));
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(pageable)
                .build();

        SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(nativeQuery, MediaDocument.class);
        SearchPage<MediaDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

        return SliceResponse.from(resultPage.map(SearchHit::getContent), MediaSearchResponse::from);
    }


    @Override
    public SliceResponse<PostSearchResponse> searchPosts(String query, Pageable pageable) {
        log.debug("Elasticsearch 게시글 검색을 시작합니다. Query: {}", query);
        if (pageable.isPaged() && pageable.getPageNumber() == 0) {
            logSearchKeyword(query);
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(mm -> mm
                                .query(query)
                                .fields("title^2", "content")
                                .type(TextQueryType.BestFields)
                        )
                )
                .withPageable(pageable)
                .build();

        SearchHits<PostDocument> searchHits = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        SearchPage<PostDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

        return SliceResponse.from(resultPage.map(SearchHit::getContent), PostSearchResponse::from);
    }

    @Override
    public SliceResponse<PersonSearchResponse> searchPeople(String query, Pageable pageable) {
        log.debug("Elasticsearch 인물 검색을 시작합니다. Query: {}", query);
        if (pageable.isPaged() && pageable.getPageNumber() == 0) {
            logSearchKeyword(query);
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field("name")
                                .query(query)
                        )
                )
                .withPageable(pageable)
                .build();

        SearchHits<PersonDocument> searchHits = elasticsearchOperations.search(nativeQuery, PersonDocument.class);
        SearchPage<PersonDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

        return SliceResponse.from(resultPage.map(SearchHit::getContent), PersonSearchResponse::from);
    }

    @Override
    public SliceResponse<UserSearchResponse> searchUsers(String nickname, Pageable pageable) {
        log.debug("Elasticsearch 사용자 검색을 시작합니다. Nickname: {}", nickname);
        logSearchKeyword(nickname);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field("nickname")
                                .query(nickname)
                        )
                )
                .withPageable(pageable)
                .build();

        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(nativeQuery, UserDocument.class);
        SearchPage<UserDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

        return SliceResponse.from(resultPage.map(SearchHit::getContent), userDoc ->
                new UserSearchResponse(userDoc.id(), userDoc.nickname(), userDoc.profileImageUrl())
        );
    }

    @Override
    public SliceResponse<CollectionSearchResponse> searchCollections(String query, Pageable pageable) {
        log.debug("Elasticsearch 컬렉션 검색을 시작합니다. Query: {}", query);
        logSearchKeyword(query);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .match(m -> m
                                .field("name")
                                .query(query)
                        )
                )
                .withPageable(pageable)
                .build();

        SearchHits<CollectionDocument> searchHits = elasticsearchOperations.search(nativeQuery, CollectionDocument.class);
        SearchPage<CollectionDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

        return SliceResponse.from(resultPage.map(SearchHit::getContent), doc ->
                new CollectionSearchResponse(doc.id(), doc.name(), doc.posterPath())
        );
    }

    @Override
    public SliceResponse<MediaSearchResponse> searchMediaByKeyword(String keyword, Pageable pageable) {
        log.debug("Elasticsearch 키워드 기반 미디어 검색을 시작합니다. Keyword: {}", keyword);
        logSearchKeyword(keyword);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .term(t -> t
                                .field("keywords")
                                .value(keyword)
                        )
                )
                .withPageable(pageable)
                .build();

        SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(nativeQuery, MediaDocument.class);
        SearchPage<MediaDocument> resultPage = org.springframework.data.elasticsearch.core.SearchHitSupport.searchPageFor(searchHits, pageable);

        return SliceResponse.from(resultPage.map(SearchHit::getContent), MediaSearchResponse::from);
    }

    @Override
    public List<String> getSearchSuggestions(String prefix) {
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(mm -> mm
                                .query(prefix)
                                .type(TextQueryType.BoolPrefix)
                                .fields("title", "title._2gram", "title._3gram")
                        )
                )
                .withMaxResults(10)
                .build();

        SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(searchQuery, MediaDocument.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAutocompletions(String prefix) {
        NativeQuery searchQuery = NativeQuery.builder()
                .withSuggester(Suggester.of(s -> s
                        .suggesters("title-suggester", fs -> fs
                                .prefix(prefix)
                                .completion(cs -> cs
                                        .field("title_suggest")
                                        .size(10)
                                )
                        )
                ))
                .build();

        SearchHits<MediaDocument> searchHits = elasticsearchOperations.search(searchQuery, MediaDocument.class);
        Suggest suggest = searchHits.getSuggest();

        if (suggest != null) {
            return suggest.getSuggestion("title-suggester").getEntries().stream()
                    .flatMap(entry -> entry.getOptions().stream())
                    .map(Suggest.Suggestion.Entry.Option::getText)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<TrendingSearchResponse> getTrendingSearches() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<String> trendingKeywords = zSetOperations.reverseRange(TRENDING_SEARCHES_KEY, 0, 9);

        if (trendingKeywords == null || trendingKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        AtomicInteger rank = new AtomicInteger(1);
        return trendingKeywords.stream()
                .map(keyword -> new TrendingSearchResponse(rank.getAndIncrement(), keyword))
                .collect(Collectors.toList());
    }

    /**
     * 검색어를 Redis Sorted Set에 기록하고 점수(검색 횟수)를 1 증가시킵니다.
     * @param query 검색어
     */
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
