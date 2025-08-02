package com.example.CineHive.domain.review;

import com.example.CineHive.domain.common.dto.ApiResponse;
import com.example.CineHive.domain.common.dto.PagedResponse;
import com.example.CineHive.domain.media.MediaType;
import com.example.CineHive.domain.review.dto.CreateReviewRequest;
import com.example.CineHive.domain.review.dto.ReviewResponse;
import com.example.CineHive.domain.review.dto.UpdateReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.net.URI;

@Tag(name = "Review Controller", description = "미디어 리뷰 CRUD API")
@Validated
@RestController
@RequestMapping("/api/v1/media/{mediaType}/{tmdbId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성",
            description = """
            ### **특정 미디어에 대한 새로운 리뷰를 작성합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[주요 규칙]**
            - 한 사용자는 동일한 미디어에 대해 하나의 리뷰만 작성할 수 있습니다. 중복 작성 시도 시 `409 Conflict` 에러가 발생합니다.
            - 별점(`rating`)은 0.5 단위로 0.5에서 5.0 사이의 값이어야 합니다.
            
            **[서버 처리]**
            1.  리뷰 대상 미디어가 우리 DB에 없으면, TMDB API에서 정보를 가져와 자동으로 저장합니다.
            2.  리뷰를 저장한 후, 해당 미디어의 평균 별점과 리뷰 수를 갱신합니다.
            
            **[응답]**
            - 성공 시 `201 Created` 상태 코드와 함께 생성된 리뷰의 상세 정보(`ReviewResponse`)를 반환합니다.
            - `Location` 헤더에 생성된 리뷰의 URI가 포함됩니다. (예: `/api/v1/reviews/{reviewId}`)
            """)
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Parameter(description = "미디어 타입 (MOVIE 또는 TV)") @PathVariable MediaType mediaType,
            @Parameter(description = "미디어의 TMDB ID") @PathVariable Integer tmdbId,
            @Valid @RequestBody CreateReviewRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        ReviewResponse response = reviewService.createReview(request, userDetails.getUsername());

        URI location = URI.create(String.format("/api/v1/reviews/%d", response.id()));
        return ResponseEntity.created(location).body(ApiResponse.ok(response));
    }

    @Operation(summary = "특정 미디어의 리뷰 목록 조회",
            description = """
            ### **특정 미디어에 달린 리뷰 목록을 페이징하여 조회합니다.**
            
            **[인증]**
            - 필요 없음 (공개 API)
            
            **[페이징 파라미터]**
            - `page`: 조회할 페이지 번호 (0부터 시작, 기본값: 0)
            - `size`: 한 페이지에 표시할 리뷰 수 (기본값: 10, 최대: 50)
            - `sort`: 정렬 기준. `createdAt,desc` (최신순), `createdAt,asc` (오래된순) 등 사용 가능.
            
            **[응답]**
            - `PagedResponse` 객체 형태로 반환되며, 리뷰 목록(`content`)과 함께 페이징 관련 정보가 포함됩니다.
            """)
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getReviewsForMedia(
            @Parameter(description = "미디어 타입 (MOVIE 또는 TV)") @PathVariable MediaType mediaType,
            @Parameter(description = "미디어의 TMDB ID") @PathVariable Integer tmdbId,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PagedResponse<ReviewResponse> response = reviewService.getReviewsForMedia(tmdbId, mediaType, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "리뷰 수정",
            description = """
            ### **자신이 작성한 리뷰의 내용과 별점을 수정합니다.**
            
            **[인증 및 권한]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            - **소유권 검증**: 오직 리뷰를 작성한 본인만 수정할 수 있습니다.
            
            **[예외 상황]**
            - 타인의 리뷰 수정 시도: `403 Forbidden`
            - 존재하지 않는 리뷰 ID: `404 Not Found`
            
            **[응답]**
            - 성공 시, 수정된 리뷰의 전체 상세 정보(`ReviewResponse`)를 반환합니다.
            """)
    @PutMapping("/{reviewId}")
    @PreAuthorize("@reviewService.isAuthor(#reviewId, principal.username)")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @Parameter(description = "미디어 타입 (경로 일관성을 위해 필요)") @PathVariable MediaType mediaType,
            @Parameter(description = "미디어의 TMDB ID") @PathVariable Integer tmdbId,
            @Parameter(description = "수정할 리뷰의 ID") @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        ReviewResponse response = reviewService.updateReview(reviewId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "리뷰 삭제",
            description = """
            ### **자신이 작성한 리뷰를 영구적으로 삭제합니다.**
            
            **[인증 및 권한]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            - **소유권 검증**: 오직 리뷰를 작성한 본인 또는 `ADMIN` 역할만 삭제할 수 있습니다.
            
            **[예외 상황]**
            - 타인의 리뷰 삭제 시도: `403 Forbidden`
            - 존재하지 않는 리뷰 ID: `404 Not Found`
            
            **[응답]**
            - 성공 시, 응답 본문 없이 `204 No Content` 상태 코드를 반환합니다.
            """)
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@reviewService.isAuthor(#reviewId, principal.username) or hasRole('ADMIN')")
    public void deleteReview(
            @Parameter(description = "미디어 타입 (경로 일관성을 위해 필요)") @PathVariable MediaType mediaType,
            @Parameter(description = "미디어의 TMDB ID") @PathVariable Integer tmdbId,
            @Parameter(description = "삭제할 리뷰의 ID") @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        reviewService.deleteReview(reviewId, userDetails.getUsername());
    }
}
