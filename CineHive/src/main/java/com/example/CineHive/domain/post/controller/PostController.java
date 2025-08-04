package com.example.CineHive.domain.post.controller;

import com.example.CineHive.domain.post.dto.*;
import com.example.CineHive.domain.report.dto.ReportRequest;
import com.example.CineHive.domain.post.service.PostService;
import com.example.CineHive.domain.post.bookmark.service.BookmarkService;
import com.example.CineHive.domain.post.dislike.service.DislikeService;
import com.example.CineHive.domain.post.like.service.LikeService;
import com.example.CineHive.domain.report.service.ReportService;
import com.example.CineHive.domain.common.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final LikeService likeService;
    private final DislikeService dislikeService;
    private final BookmarkService bookmarkService;
    private final ReportService reportService;

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
               - `page`: 페이지 번호 (0부터 시작).
               - `size`: 페이지당 게시글 수.
               - `sort`: 정렬 기준. `createdAt,desc` (최신순), `views,desc` (조회수순), `likeCount,desc` (좋아요순) 등 `프로퍼티명,정렬방향` 형식으로 요청 가능합니다.
               """)
    @GetMapping
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.PageResponse<PostSummaryResponse>>> getPosts(
            @Parameter(hidden = true)
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        com.example.CineHive.domain.common.controller.dto.PageResponse<PostSummaryResponse> pageResponse = postService.getPosts(pageable);
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(pageResponse));
    }

    @Operation(summary = "게시글 키워드 검색 페이징 조회")
    @GetMapping("/search")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.PageResponse<PostSummaryResponse>>> searchPosts(
            @RequestParam String keyword,
            @Parameter(hidden = true)
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        com.example.CineHive.domain.common.controller.dto.PageResponse<PostSummaryResponse> response = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(response));
    }

    @Operation(summary = "새 게시글 작성",
            description = """
            ### **새로운 게시글을 등록합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다. (`USER` 역할 이상)
            
            **[요청 본문]**
            - `title` (문자열): 게시글 제목 (필수)
            - `content` (문자열): 게시글 내용 (필수)
            
            **[서버 처리 및 응답]**
            - 성공 시, 생성된 게시글의 상세 정보(`PostDetailResponse`)와 함께 `201 CREATED` 상태 코드가 반환됩니다.
            """)
    @PostMapping
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse createdPost = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(createdPost));
    }

    @Operation(summary = "게시글 상세 조회",
            description = """
            ### **특정 ID를 가진 게시글의 상세 정보를 조회합니다.**
            
            **[주요 동작]**
            - **조회수 증가**: 이 API를 호출하면 해당 게시글의 조회수(`views`)가 1 증가합니다.
            - **상세 정보 반환**: 게시글의 모든 정보와 함께 댓글 목록(`comments`)도 포함되어 반환됩니다.
            """)
    @GetMapping("/{postId}")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<PostDetailResponse>> getPostById(@PathVariable Long postId) {
        PostDetailResponse postDetailResponse = postService.getPostById(postId);
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(postDetailResponse));
    }

    @Operation(summary = "게시글 수정",
            description = """
            ### **자신이 작성한 게시글의 제목과 내용을 수정합니다.**
            
            **[인증 및 권한]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            - **소유권 검증**: 게시글을 작성한 본인만 수정할 수 있습니다. 타인의 게시글 수정 시도 시 에러가 발생합니다.
            
            **[요청 본문]**
            - `title` (문자열): 새로운 게시글 제목 (필수)
            - `content` (문자열): 새로운 게시글 내용 (필수)
            
            **[응답]**
            - 성공 시, 수정된 게시글의 전체 상세 정보(`PostDetailResponse`)가 반환됩니다.
            """)
    @PutMapping("/{postId}")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse updatedPost = postService.updatePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(updatedPost));
    }

    @Operation(summary = "게시글 삭제",
            description = """
            ### **자신이 작성한 게시글을 영구적으로 삭제합니다.**
            
            **[인증 및 권한]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            - **소유권 검증**: 게시글을 작성한 본인 또는 `ADMIN` 역할만 삭제할 수 있습니다.
            
            **[응답]**
            - 성공 시, "게시글이 성공적으로 삭제되었습니다." 메시지를 담은 `MessageResponse`가 반환됩니다.
            """)
    @DeleteMapping("/{postId}")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> deletePost(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("게시글이 성공적으로 삭제되었습니다.")));
    }

    // =========================================
    // == 게시글 상호작용
    // =========================================

    @Operation(summary = "게시글 좋아요",
            description = """
            ### **특정 게시글에 '좋아요'를 누릅니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[주요 동작]**
            - 이미 '좋아요'를 누른 상태에서 다시 호출해도 아무 변화가 없습니다.
            - 이미 '싫어요'를 누른 상태였다면, '싫어요'는 자동으로 취소되고 '좋아요'가 등록됩니다.
            """)
    @PostMapping("/{postId}/likes")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> addLike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        likeService.addLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("게시글에 '좋아요'를 눌렀습니다.")));
    }

    @Operation(summary = "게시글 '좋아요' 취소",
            description = """
            ### **특정 게시글에 눌렀던 '좋아요'를 취소합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[주요 동작]**
            - '좋아요'를 누른 기록이 있는 경우에만 정상적으로 취소됩니다.
            """)
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> removeLike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        likeService.removeLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("'좋아요'를 취소했습니다.")));
    }

    @Operation(summary = "게시글 싫어요",
            description = """
            ### **특정 게시글에 '싫어요'를 누릅니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[주요 동작]**
            - 이미 '싫어요'를 누른 상태에서 다시 호출해도 아무 변화가 없습니다.
            - 이미 '좋아요'를 누른 상태였다면, '좋아요'는 자동으로 취소되고 '싫어요'가 등록됩니다.
            """)
    @PostMapping("/{postId}/dislikes")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> addDislike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        dislikeService.addDislike(postId, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("게시글에 '싫어요'를 눌렀습니다.")));
    }

    @Operation(summary = "게시글 '싫어요' 취소",
            description = """
            ### **특정 게시글에 눌렀던 '싫어요'를 취소합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[주요 동작]**
            - '싫어요'를 누른 기록이 있는 경우에만 정상적으로 취소됩니다.
            """)
    @DeleteMapping("/{postId}/dislikes")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> removeDislike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        dislikeService.removeDislike(postId, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("'싫어요'를 취소했습니다.")));
    }

    @Operation(summary = "게시글 북마크",
            description = """
            ### **특정 게시글을 북마크 목록에 추가합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[주요 동작]**
            - 이미 북마크한 게시글을 다시 요청하면 중복으로 추가되지 않고, 에러(예: 409 Conflict)가 발생할 수 있습니다.
            """)
    @PostMapping("/{postId}/bookmarks")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> addBookmark(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        bookmarkService.addBookmark(postId, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("게시글을 북마크했습니다.")));
    }

    @Operation(summary = "게시글 북마크 취소",
            description = """
            ### **북마크 목록에서 특정 게시글을 삭제합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[주요 동작]**
            - 북마크한 기록이 있는 게시글에 대해서만 정상적으로 취소됩니다.
            """)
    @DeleteMapping("/{postId}/bookmarks")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> removeBookmark(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        bookmarkService.removeBookmark(postId, userDetails.getUsername());
        return ResponseEntity.ok(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("북마크를 취소했습니다.")));
    }

    @Operation(summary = "게시글 신고",
            description = """
            ### **부적절한 게시글을 신고합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[요청 본문]**
            - `reason` (문자열): 신고 사유 (필수)
            
            **[주요 규칙]**
            - 자신의 게시글은 신고할 수 없습니다.
            - 동일한 게시글을 중복해서 신고할 수 없습니다.
            
            **[응답]**
            - 성공 시, 신고 접수 완료 메시지와 함께 `201 CREATED` 상태 코드가 반환됩니다.
            """)
    @PostMapping("/{postId}/reports")
    public ResponseEntity<com.example.CineHive.domain.common.controller.dto.ApiResponse<com.example.CineHive.domain.common.controller.dto.MessageResponse>> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        reportService.reportPost(postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.example.CineHive.domain.common.controller.dto.ApiResponse.ok(new com.example.CineHive.domain.common.controller.dto.MessageResponse("게시글 신고가 정상적으로 접수되었습니다.")));
    }
}