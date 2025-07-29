package com.example.CineHive.domain.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 콘텐츠 관리 API 컨트롤러입니다.
 */
@Tag(name = "Admin Content Controller", description = "관리자용 콘텐츠 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminContentController {

    @Operation(summary = "전체 게시글 목록 조회")
    @GetMapping("/posts")
    public void getAllPosts() {
        // TODO: AdminContentService.getPosts(searchCondition, pageable) 호출
    }

    @Operation(summary = "게시글 직접 수정")
    @PatchMapping("/posts/{postId}")
    public void updatePostByAdmin(@PathVariable Long postId) {
        // TODO: AdminContentService.updatePost(postId, request) 호출
    }

    @Operation(summary = "게시글 상태 변경 (숨김/노출)")
    @PatchMapping("/posts/{postId}/status")
    public void updatePostStatus(@PathVariable Long postId) {
        // TODO: AdminContentService.updatePostStatus(postId, request) 호출
    }

    @Operation(summary = "게시글 강제 삭제")
    @DeleteMapping("/posts/{postId}")
    public void deletePostByAdmin(@PathVariable Long postId) {
        // TODO: AdminContentService.deletePost(postId) 호출
    }

    @Operation(summary = "댓글 상태 변경 (숨김/노출)")
    @PatchMapping("/comments/{commentId}/status")
    public void updateCommentStatus(@PathVariable Long commentId) {
        // TODO: AdminContentService.updateCommentStatus(commentId, request) 호출
    }

    @Operation(summary = "댓글 강제 삭제")
    @DeleteMapping("/comments/{commentId}")
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        // TODO: AdminContentService.deleteComment(commentId) 호출
    }

    @Operation(summary = "리뷰 강제 삭제")
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReviewByAdmin(@PathVariable Long reviewId) {
        // TODO: AdminContentService.deleteReview(reviewId) 호출
    }

    @Operation(summary = "플레이리스트 강제 삭제")
    @DeleteMapping("/playlists/{playlistId}")
    public void deletePlaylistByAdmin(@PathVariable Long playlistId) {
        // TODO: AdminContentService.deletePlaylist(playlistId) 호출
    }
}