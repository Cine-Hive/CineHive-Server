package com.example.CineHive.controller.post;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.global.PagedResponse;
import com.example.CineHive.dto.post.*;
import com.example.CineHive.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글(Posts)의 CRUD, 트렌드 및 상호작용을 담당하는 API 컨트롤러입니다.
 */
@Tag(name = "Post Controller", description = "게시글 CRUD, 트렌드, 상호작용 API")
@Validated
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    // TODO: private final LikeService likeService;
    // TODO: private final DislikeService dislikeService;
    // TODO: private final BookmarkService bookmarkService;
    // TODO: private final ReportService reportService;

    // =========================================
    // == 게시글 조회 및 생성
    // =========================================

    @Operation(summary = "인기 게시글 목록 조회")
    @GetMapping("/trends/popular")
    public void getPopularPosts() {
        // TODO: 1. PostService에서 인기 게시글 목록 조회 (페이징)
        // TODO: 2. PagedResponse<PostSummaryResponse> 형태로 반환
    }

    @Operation(summary = "주간 인기 게시글 목록 조회")
    @GetMapping("/trends/weekly")
    public void getWeeklyPopularPosts() {
        // TODO: 1. PostService에서 주간 인기 게시글 목록 조회 (페이징)
        // TODO: 2. PagedResponse<PostSummaryResponse> 형태로 반환
    }

    @Operation(summary = "게시글 카테고리 목록 조회")
    @GetMapping("/categories")
    public void getPostCategories() {
        // TODO: 1. PostService 또는 별도의 CategoryService에서 카테고리 목록 조회
        // TODO: 2. CategoryResponse (신규 DTO) 리스트로 변환하여 반환
    }

    @Operation(summary = "게시글 목록 페이징 조회",
            description = """
               게시글 목록을 페이징하여 조회합니다.
               - `page`: 페이지 번호 (1부터 시작).
               - `size`: 페이지당 게시글 수.
               - `sort`: 정렬 기준. `LATEST`(최신순), `VIEWS`(조회수순), `LIKES`(좋아요순) 중 선택 가능합니다.
               """)
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostSummaryResponse>>> getPosts(
            @Parameter(description = "페이지 번호 (1부터 시작)")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "페이지당 게시글 수")
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "정렬 기준", schema = @Schema(implementation = PostSortType.class))
            @RequestParam(defaultValue = "LATEST") PostSortType sort) {
        PagedResponse<PostSummaryResponse> pagedResponse = postService.getPosts(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(pagedResponse));
    }

    @Operation(summary = "새 게시글 작성",
            description = """
               새로운 게시글을 등록합니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               - 요청 본문에 `title`과 `content`는 필수입니다.
               - 성공 시, 생성된 게시글의 상세 정보와 함께 `201 CREATED` 상태 코드가 반환됩니다.
               """)
    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse createdPost = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createdPost));
    }

    @Operation(summary = "게시글 상세 조회",
            description = """
               특정 ID를 가진 게시글의 상세 정보를 조회합니다.
               - **Side Effect**: 이 API를 호출하면 해당 게시글의 조회수(`views`)가 1 증가합니다.
               - 댓글 목록(`comments`)도 함께 포함되어 반환됩니다.
               """)
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostById(@PathVariable Long postId) {
        PostDetailResponse postDetailResponse = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.ok(postDetailResponse));
    }

    @Operation(summary = "게시글 수정",
            description = """
               자신이 작성한 게시글의 제목과 내용을 수정합니다.
               - **인증 및 권한 필요**: 게시글을 작성한 본인만 수정할 수 있습니다. 타인의 게시글 수정 시도 시 `403 Forbidden` 에러가 발생합니다.
               - 성공 시, 수정된 게시글의 전체 상세 정보가 반환됩니다.
               """)
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse updatedPost = postService.updatePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(updatedPost));
    }

    @Operation(summary = "게시글 삭제",
            description = """
               자신이 작성한 게시글을 영구적으로 삭제합니다.
               - **인증 및 권한 필요**: 게시글을 작성한 본인 또는 `ADMIN` 역할만 삭제할 수 있습니다. 타인의 게시글 삭제 시도 시 `403 Forbidden` 에러가 발생합니다.
               - 성공 시, 성공 메시지를 담은 응답이 반환됩니다.
               """)
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deletePost(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("게시글이 성공적으로 삭제되었습니다.")));
    }

    // =========================================
    // == 게시글 상호작용
    // =========================================

    @Operation(summary = "게시글 좋아요")
    @PostMapping("/{postId}/likes")
    public void addLike(@PathVariable Long postId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. LikeService.addLike(postId, userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "게시글 좋아요 취소")
    @DeleteMapping("/{postId}/likes")
    public void removeLike(@PathVariable Long postId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. LikeService.removeLike(postId, userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "게시글 싫어요")
    @PostMapping("/{postId}/dislikes")
    public void addDislike(@PathVariable Long postId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. DislikeService.addDislike(postId, userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "게시글 싫어요 취소")
    @DeleteMapping("/{postId}/dislikes")
    public void removeDislike(@PathVariable Long postId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. DislikeService.removeDislike(postId, userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "게시글 북마크")
    @PostMapping("/{postId}/bookmarks")
    public void addBookmark(@PathVariable Long postId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. BookmarkService.addBookmark(postId, userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "게시글 북마크 취소")
    @DeleteMapping("/{postId}/bookmarks")
    public void removeBookmark(@PathVariable Long postId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. BookmarkService.removeBookmark(postId, userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "게시글 신고")
    @PostMapping("/{postId}/reports")
    public void reportPost(@PathVariable Long postId, @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. ReportRequest DTO를 @RequestBody로 받음
        // TODO: 2. ReportService.reportPost(postId, request, userEmail) 호출
        // TODO: 3. 성공(201 CREATED) 시 MessageResponse 반환
    }
}