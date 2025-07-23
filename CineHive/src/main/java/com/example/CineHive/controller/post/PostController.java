package com.example.CineHive.controller.post;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.global.PagedResponse;
import com.example.CineHive.dto.post.CreatePostRequest;
import com.example.CineHive.dto.post.PostDetailResponse;
import com.example.CineHive.dto.post.PostSortType;
import com.example.CineHive.dto.post.PostSummaryResponse;
import com.example.CineHive.dto.post.UpdatePostRequest;
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

@Tag(name = "Post Controller", description = "게시글 CRUD 및 조회 API")
@Validated
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse createdPost = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createdPost));
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostById(@PathVariable Long postId) {
        PostDetailResponse postDetailResponse = postService.getPostById(postId);
        return ResponseEntity.ok(ApiResponse.ok(postDetailResponse));
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse updatedPost = postService.updatePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(updatedPost));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deletePost(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("게시글이 성공적으로 삭제되었습니다.")));
    }

    @Operation(summary = "게시글 목록 페이징 조회")
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
}