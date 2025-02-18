package com.example.CineHive.controller.boardController;


import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.board.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bookmark")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    // 북마크 추가
    @PostMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> addBookmark(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isBookmarked = bookmarkService.addBookmark(memEmail, boardId);
        return ResponseEntity.ok(isBookmarked ? "Bookmarked" : "Already Exists");
    }

    // 북마크 삭제
    @DeleteMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> removeBookmark(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isRemoved = bookmarkService.removeBookmark(memEmail, boardId);
        return isRemoved ? ResponseEntity.ok("Unbookmarked") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bookmark Not Found");
    }

    // 특정 게시글의 북마크 개수 조회
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getBookmarkCount(@PathVariable Long boardId) {
        int bookmarkCount = bookmarkService.getBookmarkCount(boardId);
        return ResponseEntity.ok(bookmarkCount);
    }
}