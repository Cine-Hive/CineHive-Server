package com.example.CineHive.controller.post;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.post.BookmarkCountResponse;
import com.example.CineHive.dto.post.BookmarkStatusResponse;
import com.example.CineHive.service.post.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Bookmark Controller", description = "게시글 북마크 관련 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "게시글 북마크 추가")
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> addBookmark(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        bookmarkService.addBookmark(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("게시글을 북마크했습니다.")));
    }

    @Operation(summary = "게시글 북마크 취소")
    @DeleteMapping
    public ResponseEntity<ApiResponse<MessageResponse>> removeBookmark(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        bookmarkService.removeBookmark(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("북마크를 취소했습니다.")));
    }

    @Operation(summary = "게시글 북마크 개수 조회")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<BookmarkCountResponse>> getBookmarkCount(@PathVariable Long postId) {
        int count = bookmarkService.getBookmarkCount(postId);
        return ResponseEntity.ok(ApiResponse.ok(new BookmarkCountResponse(count)));
    }

    @Operation(summary = "사용자의 북마크 여부 확인")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<BookmarkStatusResponse>> getBookmarkStatus(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        boolean isBookmarked = false;
        if (userDetails != null) {
            isBookmarked = bookmarkService.isBookmarkedByUser(postId, userDetails.getUsername());
        }
        return ResponseEntity.ok(ApiResponse.ok(new BookmarkStatusResponse(isBookmarked)));
    }
}