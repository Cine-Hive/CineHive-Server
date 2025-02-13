package com.example.CineHive.controller.boardController;


import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.board.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    // 북마크 등록
    @PostMapping("/bookmark/add")
    public ResponseEntity<String> addBookmark(@RequestParam String memEmail, @RequestParam Long boardId) {
        boolean isBookmarked = bookmarkService.addBookmark(memEmail, boardId);
        return ResponseEntity.ok(isBookmarked ? "Bookmarked" : "Already Exists");
    }

    // 북마크 삭제
    @DeleteMapping("/bookmark/remove")
    public ResponseEntity<String> removeBookmark(@RequestParam String memEmail, @RequestParam Long boardId) {
        boolean isRemoved = bookmarkService.removeBookmark(memEmail, boardId);
        return isRemoved ? ResponseEntity.ok("Unbookmarked") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bookmark Not Found");
    }
    @GetMapping("/board/bookmark/{id}/count")
    public ResponseEntity<Integer> getBookmarkCount(@PathVariable Long id) {
        int bookmarkCount = bookmarkService.getBookmarkCount(id);
        return ResponseEntity.ok(bookmarkCount); // 북마크 수 반환
    }
}