package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean addBookmark(String memEmail, Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // 북마크가 이미 존재하지 않으면 새로운 북마크 추가
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndBoard(user, board);
        if (existingBookmark.isPresent()) {
            return false; 
        } else {
            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setBoard(board);
            bookmarkRepository.save(bookmark);
            // 북마크 수 갱신
            board.updateBookmarkCount();
            return true; // 북마크 추가 후 true 반환
        }
    }

    @Transactional
    public boolean removeBookmark(String memEmail, Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndBoard(user, board);

        if (existingBookmark.isPresent()) {
            Bookmark bookmark = existingBookmark.get();
            bookmarkRepository.delete(bookmark);

            bookmarkRepository.flush();  // DB에 즉시 반영되도록 하는 함수

            board.updateBookmarkCount();
            boardRepository.save(board);
            return true;
        } else {
            return false;
        }
    }

    public int getBookmarkCount(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        return board.getBookmarkCount();
    }

}
