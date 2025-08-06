package com.example.CineHive.domain.search.controller;

import com.example.CineHive.domain.media.enums.MediaType;
import com.example.CineHive.domain.search.dto.*;
import com.example.CineHive.domain.search.service.SearchService;
import com.example.CineHive.global.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 서비스의 모든 검색 기능을 제공하는 API 컨트롤러입니다.
 */
@Tag(name = "Search Controller", description = "통합 검색 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "통합 검색",
            description = """
            ### **하나의 검색어로 여러 도메인의 정보를 한번에 조회합니다.**
            - 미디어(영화/TV), 게시글, 인물 정보를 동시에 검색하여 각 상위 5개 결과를 반환합니다.
            - 각 도메인별 검색은 서버 내부에서 병렬로 처리되어 빠른 응답 속도를 제공합니다.
            
            **[클라이언트 처리]**
            1.  사용자가 검색창에 검색어를 입력하고 실행했을 때 호출하기 가장 적합한 API입니다.
            2.  응답받은 `media`, `posts`, `people` 목록을 각각의 UI 섹션에 표시합니다.
            3.  각 섹션 하단에 '더보기' 버튼을 두어, 사용자가 특정 도메인의 검색 결과를 더 보고 싶을 때 각 상세 검색 API(예: `/api/v1/search/media`)를 호출하도록 유도할 수 있습니다.
            """)
    @GetMapping("/all")
    public SearchAllResponse searchAll(
            @Parameter(description = "검색어") @RequestParam String query) throws ExecutionException, InterruptedException {
        return searchService.searchAll(query).get();
    }

    @Operation(summary = "미디어(영화/TV) 검색",
            description = """
            ### **영화와 TV 시리즈 정보를 키워드로 검색합니다.**
            - `query` 파라미터로 제목, 줄거리 등을 기준으로 검색합니다.
            - `mediaType` 파라미터(`movie` 또는 `tv`)를 통해 특정 타입의 미디어만 필터링할 수 있습니다. (미지정 시 전체 검색)
            - 응답은 무한 스크롤에 최적화된 `Slice` 형태로 제공됩니다.
            
            **[클라이언트 처리]**
            1.  '더보기'를 통해 미디어 검색 결과 페이지로 진입했을 때 사용합니다.
            2.  `hasNext`가 `true`일 경우, 다음 페이지를 요청할 수 있습니다. 다음 요청 시 `page` 파라미터를 1 증가시켜 호출합니다.
            """)
    @GetMapping("/media")
    public SliceResponse<MediaSearchResponse> searchMedia(
            @Parameter(description = "검색어") @RequestParam String query,
            @Parameter(description = "미디어 타입 (movie, tv)", required = false) @RequestParam(required = false) MediaType mediaType,
            @ParameterObject Pageable pageable) {
        return searchService.searchMedia(query, mediaType, pageable);
    }

    @Operation(summary = "게시글 검색",
            description = """
            ### **게시글의 제목과 내용에서 키워드로 검색합니다.**
            - 응답은 무한 스크롤에 최적화된 `Slice` 형태로 제공됩니다.
            
            **[클라이언트 처리]**
            1.  '더보기'를 통해 게시글 검색 결과 페이지로 진입했을 때 사용합니다.
            2.  `hasNext`가 `true`일 경우, 다음 페이지를 요청할 수 있습니다. 다음 요청 시 `page` 파라미터를 1 증가시켜 호출합니다.
            """)
    @GetMapping("/posts")
    public SliceResponse<PostSearchResponse> searchPosts(
            @Parameter(description = "검색어") @RequestParam String query,
            @ParameterObject Pageable pageable) {
        return searchService.searchPosts(query, pageable);
    }

    @Operation(summary = "인물 검색",
            description = """
            ### **배우, 감독 등 인물의 이름으로 검색합니다.**
            - 응답은 무한 스크롤에 최적화된 `Slice` 형태로 제공됩니다.
            
            **[클라이언트 처리]**
            1.  '더보기'를 통해 인물 검색 결과 페이지로 진입했을 때 사용합니다.
            2.  `hasNext`가 `true`일 경우, 다음 페이지를 요청할 수 있습니다. 다음 요청 시 `page` 파라미터를 1 증가시켜 호출합니다.
            """)
    @GetMapping("/people")
    public SliceResponse<PersonSearchResponse> searchPeople(
            @Parameter(description = "검색어") @RequestParam String query,
            @ParameterObject Pageable pageable) {
        return searchService.searchPeople(query, pageable);
    }

    @Operation(summary = "사용자 검색",
            description = """
            ### **서비스에 가입된 사용자를 닉네임으로 검색합니다.**
            - 응답은 무한 스크롤에 최적화된 `Slice` 형태로 제공됩니다.
            
            **[클라이언트 처리]**
            1.  사용자 검색 결과 페이지에서 사용합니다.
            2.  `hasNext`가 `true`일 경우, 다음 페이지를 요청할 수 있습니다. 다음 요청 시 `page` 파라미터를 1 증가시켜 호출합니다.
            """)
    @GetMapping("/users")
    public SliceResponse<UserSearchResponse> searchUsers(
            @Parameter(description = "사용자 닉네임") @RequestParam String nickname,
            @ParameterObject Pageable pageable) {
        return searchService.searchUsers(nickname, pageable);
    }

    @Operation(summary = "컬렉션 검색 (구현 예정)",
            description = """
            ### **사용자들이 만든 컬렉션을 검색합니다. (현재는 비어있는 목록을 반환합니다)**
            """)
    @GetMapping("/collections")
    public SliceResponse<CollectionSearchResponse> searchCollections(
            @Parameter(description = "검색어") @RequestParam String query,
            @ParameterObject Pageable pageable) {
        return searchService.searchCollections(query, pageable);
    }

    @Operation(summary = "키워드 기반 미디어 검색",
            description = """
            ### **특정 키워드(태그)를 포함하는 미디어 목록을 조회합니다.**
            - 예: '마블', 'DC 코믹스' 등 특정 주제와 관련된 영화/TV 시리즈를 찾을 때 유용합니다.
            - 응답은 무한 스크롤에 최적화된 `Slice` 형태로 제공됩니다.
            
            **[클라이언트 처리]**
            1.  미디어 상세 페이지 등에서 특정 키워드(태그)를 클릭했을 때, 해당 키워드를 포함하는 다른 미디어 목록을 보여주는 페이지에서 사용합니다.
            2.  `hasNext`가 `true`일 경우, 다음 페이지를 요청할 수 있습니다. 다음 요청 시 `page` 파라미터를 1 증가시켜 호출합니다.
            """)
    @GetMapping("/keywords")
    public SliceResponse<MediaSearchResponse> searchMediaByKeyword(
            @Parameter(description = "키워드(태그)") @RequestParam String keyword,
            @ParameterObject Pageable pageable) {
        return searchService.searchMediaByKeyword(keyword, pageable);
    }

    @Operation(summary = "자동완성 추천어 (유연한 검색)",
            description = """
            ### **사용자가 입력하는 검색어에 따라 실시간으로 미디어 제목을 추천합니다.**
            - 'search_as_you_type' 데이터 타입을 사용하여, 오타나 순서가 다른 검색어에도 유연하게 반응합니다.
            - 예: '어벤져스 엔드' -> '어벤져스: 엔드게임' 추천
            
            **[클라이언트 처리]**
            1.  사용자가 검색창에 한 글자씩 입력할 때마다 이 API를 호출합니다. (Debounce 적용 권장)
            2.  반환된 문자열 목록을 검색창 아래 드롭다운 메뉴로 보여줍니다.
            """)
    @GetMapping("/suggest")
    public List<String> getSearchSuggestions(@RequestParam String prefix) {
        return searchService.getSearchSuggestions(prefix);
    }

    @Operation(summary = "자동완성 추천어 (빠른 접두사 검색)",
            description = """
            ### **사용자가 입력하는 검색어의 접두사를 기준으로 매우 빠르게 제목을 추천합니다.**
            - 'completion suggester'를 사용하여, 접두사 일치 검색에 최적화된 빠른 속도를 제공합니다.
            - 예: '어벤' -> '어벤져스', '어벤져스: 에이지 오브 울트론' 등 추천
            
            **[클라이언트 처리]**
            1.  `suggest` API와 동일하게 사용자가 검색창에 입력할 때마다 호출하여 결과를 드롭다운으로 보여줄 수 있습니다.
            2.  둘 중 하나의 API를 선택하거나, 필요에 따라 혼합하여 사용할 수 있습니다.
            """)
    @GetMapping("/autocomplete")
    public List<String> getAutocompletions(@RequestParam String prefix) {
        return searchService.getAutocompletions(prefix);
    }

    @Operation(summary = "인기 검색어 목록 조회",
            description = """
            ### **최근 가장 많이 검색된 키워드 상위 10개를 조회합니다.**
            - 서버(Redis)에 기록된 검색 로그를 기반으로 실시간 집계된 순위를 제공합니다.
            
            **[클라이언트 처리]**
            1.  사용자가 검색창을 클릭했을 때, 또는 검색 페이지의 '인기 검색어' 섹션에 순위와 함께 목록을 표시합니다.
            2.  각 키워드를 클릭하면 해당 키워드로 통합 검색(`/api/v1/search/all`)을 실행하도록 연결할 수 있습니다.
            """)
    @GetMapping("/trends")
    public List<TrendingSearchResponse> getTrendingSearches() {
        return searchService.getTrendingSearches();
    }
}
