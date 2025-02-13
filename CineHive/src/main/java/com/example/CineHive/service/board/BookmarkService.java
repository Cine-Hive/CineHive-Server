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
    public boolean toggleBookmark(String memEmail, Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndBoard(user, board);

        if (existingBookmark.isPresent()) {
            // 북마크가 이미 있으면 삭제
            bookmarkRepository.delete(existingBookmark.get());
            board.updateBookmarkCount(); // 북마크 수 업데이트
            boardRepository.save(board); // Board 엔티티 저장
            return false; // 북마크 삭제됨
        } else {
            // 없으면 추가
            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setBoard(board);
            bookmarkRepository.save(bookmark);
            board.updateBookmarkCount(); // 북마크 수 업데이트
            boardRepository.save(board); // Board 엔티티 저장
            return true; // 북마크 추가됨
        }
    }




    public int getBookmarkCount(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        return board.getBookmarkCount();
    }

}
