package com.example.CineHive.controller.boardController;


import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.board.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Bookmark Controller", description = "게시글의 즐겨찾기를 등록, 취소 및 전체 갯수 조회 기능을 제공하는 API")
@RestController
@RequestMapping("/bookmark")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    // 북마크 추가
    @Operation(summary = "즐겨찾기 등록", description = "특정 게시글의 즐겨찾기를 등록")
    @PostMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> addBookmark(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isBookmarked = bookmarkService.addBookmark(memEmail, boardId);
        return ResponseEntity.ok(isBookmarked ? "Bookmarked" : "Already Exists");
    }

    // 북마크 삭제
    @Operation(summary = "즐겨찾기 취소", description = "특정 게시글에 대해 등록한 즐겨찾기를 삭제")
    @DeleteMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> removeBookmark(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isRemoved = bookmarkService.removeBookmark(memEmail, boardId);
        return isRemoved ? ResponseEntity.ok("Unbookmarked") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bookmark Not Found");
    }

    // 특정 게시글의 북마크 개수 조회
    @Operation(summary = "즐겨찾기 수 조회 ", description = "특정 게시글에 대해 전체 즐겨찾기 수 조회")
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getBookmarkCount(@PathVariable Long boardId) {
        int bookmarkCount = bookmarkService.getBookmarkCount(boardId);
        return ResponseEntity.ok(bookmarkCount);
    }
}