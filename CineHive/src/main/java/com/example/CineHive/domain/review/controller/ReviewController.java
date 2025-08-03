package com.example.CineHive.domain.review.controller.entity;

import com.example.CineHive.domain.common.dto.ApiResponse;
import com.example.CineHive.domain.common.dto.SliceResponse;
import com.example.CineHive.domain.media.controller.MediaType;
import com.example.CineHive.domain.review.dto.CreateReviewRequest;
import com.example.CineHive.domain.review.dto.ReviewResponse;
import com.example.CineHive.domain.review.dto.UpdateReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Review Controller", description = "미디어 리뷰 CRUD API")
@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성",
            description = """
            ### **특정 미디어에 대한 새로운 리뷰를 작성합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[응답]**
            - 성공 시 `201 Created` 상태 코드와 함께 생성된 리뷰의 상세 정보(`ReviewResponse`)를 반환합니다.
            - `Location` 헤더에 생성된 리뷰의 URI가 포함됩니다. (예: `/api/v1/reviews/{reviewId}`)
            """)
    @PostMapping("/media/{mediaType}/{tmdbId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @Parameter(description = "미디어 타입 (MOVIE 또는 TV)", example = "MOVIE", required = true)
            @PathVariable MediaType mediaType,
            @Parameter(description = "미디어의 TMDB 고유 ID", example = "550", required = true)
            @PathVariable Integer tmdbId,
            @Valid @RequestBody CreateReviewRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        ReviewResponse response = reviewService.createReview(tmdbId, mediaType, request, userDetails.getUsername());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/reviews/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "특정 미디어의 리뷰 목록 조회 (무한 스크롤용)",
            description = """
            ### **특정 미디어에 달린 리뷰 목록을 슬라이스하여 조회합니다.**
            
            **[성능]**
            - 이 API는 '더보기' 또는 '무한 스크롤' UI에 최적화되어, 불필요한 전체 개수(`COUNT`) 쿼리를 실행하지 않아 성능상 이점이 있습니다.
            
            **[페이징 파라미터]**
            - `page`: 조회할 페이지 번호 (**0부터 시작**, 기본값: 0)
            - `size`: 한 페이지에 표시할 리뷰 수 (기본값: 10, 최대: 50)
            - `sort`: 정렬 기준. `createdAt,desc` (최신순), `createdAt,asc` (오래된순), `rating,desc` (별점 높은순) 등 사용 가능.
            
            **[응답]**
            - `SliceResponse` 객체 형태로 반환되며, `hasNext` 필드를 통해 다음 페이지 유무를 확인할 수 있습니다.
            """)
    @Parameters({
            @Parameter(name = "page", description = "조회할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "한 페이지에 표시할 항목 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(name = "sort", description = "정렬 기준 (예: createdAt,desc)", example = "createdAt,desc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping("/media/{mediaType}/{tmdbId}/reviews")
    public SliceResponse<ReviewResponse> getReviewsForMedia(
            @Parameter(description = "미디어 타입 (MOVIE 또는 TV)", example = "MOVIE", required = true)
            @PathVariable MediaType mediaType,
            @Parameter(description = "미디어의 TMDB 고유 ID", example = "550", required = true)
            @PathVariable Integer tmdbId,
            @Parameter(hidden = true) @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return reviewService.getReviewsForMedia(tmdbId, mediaType, pageable);
    }

    @Operation(summary = "리뷰 수정",
            description = """
            ### **자신이 작성한 리뷰의 내용과 별점을 수정합니다.**
            
            **[인증 및 권한]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            - **소유권 검증**: 오직 리뷰를 작성한 본인만 수정할 수 있습니다.
            
            **[응답]**
            - **수정된 부분**: 성공 시, 응답 본문 없이 `204 No Content` 상태 코드를 반환합니다.
            """)
    @PutMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@reviewService.isAuthor(#reviewId, principal.username)")
    public void updateReview(
                              @Parameter(description = "수정할 리뷰의 고유 ID", example = "1", required = true)
                              @PathVariable Long reviewId,
                              @Valid @RequestBody UpdateReviewRequest request,
                              @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        reviewService.updateReview(reviewId, request, userDetails.getUsername());
    }

    @Operation(summary = "리뷰 삭제",
            description = """
            ### **자신이 작성한 리뷰를 영구적으로 삭제합니다.**
            
            **[인증 및 권한]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            - **소유권 검증**: 오직 리뷰를 작성한 본인 또는 `ADMIN` 역할만 삭제할 수 있습니다.
            
            **[응답]**
            - 성공 시, 응답 본문 없이 `204 No Content` 상태 코드를 반환합니다.
            """)
    @DeleteMapping("/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@reviewService.isAuthor(#reviewId, principal.username) or hasRole('ADMIN')")
    public void deleteReview(
            @Parameter(description = "삭제할 리뷰의 고유 ID", example = "1", required = true)
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUsername());
    }
}