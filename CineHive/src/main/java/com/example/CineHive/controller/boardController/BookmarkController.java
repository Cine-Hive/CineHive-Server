package com.example.CineHive.controller.boardController;


import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.board.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private UserRepository userRepository;
    @PostMapping("/bookmark/toggle")
    public ResponseEntity<String> toggleBookmark(@RequestParam String memEmail, @RequestParam Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isBookmarked = bookmarkService.toggleBookmark(user.getMemEmail(), boardId);
        return ResponseEntity.ok(isBookmarked ? "Bookmarked" : "Unbookmarked");
    }

    @GetMapping("/board/bookmark/{id}/count")
    public ResponseEntity<Integer> getBookmarkCount(@PathVariable Long id) {
        int bookmarkCount = bookmarkService.getBookmarkCount(id);
        return ResponseEntity.ok(bookmarkCount); // 북마크 수 반환
    }
}