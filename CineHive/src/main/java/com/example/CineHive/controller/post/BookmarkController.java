package com.example.CineHive.controller.post;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.service.post.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Bookmark Controller", description = "게시글 북마크 관련 API")
@RestController
@RequestMapping("/api/v1/boards/{boardId}/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "게시글 북마크 추가", description = "특정 게시글을 북마크합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> addBookmark(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        bookmarkService.addBookmark(boardId, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "게시글을 북마크했습니다.")));
    }

    @Operation(summary = "게시글 북마크 취소", description = "특정 게시글의 북마크를 취소합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> removeBookmark(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        // [수정된 부분] 파라미터 순서를 올바르게 전달합니다.
        bookmarkService.removeBookmark(boardId, userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "북마크를 취소했습니다.")));
    }

    @Operation(summary = "게시글 북마크 개수 조회", description = "특정 게시글의 북마크 개수를 조회합니다.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getBookmarkCount(@PathVariable Long boardId) {
        int count = bookmarkService.getBookmarkCount(boardId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("bookmarkCount", count)));
    }

    @Operation(summary = "사용자의 북마크 여부 확인", description = "현재 로그인한 사용자가 특정 게시글을 북마크했는지 확인합니다. 비로그인 사용자는 항상 false를 반환합니다.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getBookmarkStatus(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        boolean isBookmarked = false;

        if (userDetails != null) {
            isBookmarked = bookmarkService.isBookmarkedByUser(boardId, userDetails.getUsername());
        }

        return ResponseEntity.ok(ApiResponse.ok(Map.of("isBookmarked", isBookmarked)));
    }
}