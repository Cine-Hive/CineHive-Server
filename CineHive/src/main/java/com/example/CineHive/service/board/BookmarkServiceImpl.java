package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.exception.BookmarkAlreadyExistsException;
import com.example.CineHive.exception.BookmarkNotFoundException;
import com.example.CineHive.exception.MemberNotFoundException;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.BookmarkRepository;
import com.example.CineHive.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    /**
     * 특정 게시글에 북마크를 추가합니다.
     *
     * @param boardId     북마크할 게시글의 ID
     * @param memberEmail 북마크를 추가하는 회원의 이메일
     * @throws BookmarkAlreadyExistsException 이미 해당 회원이 게시글을 북마크한 경우
     */
    @Override
    @Transactional
    public void addBookmark(Long boardId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        if (bookmarkRepository.existsByMemberAndBoard(member, board)) {
            throw new BookmarkAlreadyExistsException(member.getId(), board.getId());
        }

        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .board(board)
                .build();
        bookmarkRepository.save(bookmark);

        board.increaseBookmarkCount(); // 엔티티의 비즈니스 로직 호출
        log.info("Member {} bookmarked board {}", member.getId(), board.getId());
    }

    /**
     * 특정 게시글의 북마크를 제거합니다.
     *
     * @param boardId     북마크를 제거할 게시글의 ID
     * @param memberEmail 북마크를 제거하는 회원의 이메일
     * @throws BookmarkNotFoundException 해당 회원이 게시글을 북마크하지 않은 경우
     */
    @Override
    @Transactional
    public void removeBookmark(Long boardId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        Bookmark bookmark = bookmarkRepository.findByMemberAndBoard(member, board)
                .orElseThrow(() -> new BookmarkNotFoundException(member.getId(), board.getId()));

        bookmarkRepository.delete(bookmark);

        board.decreaseBookmarkCount(); // 엔티티의 비즈니스 로직 호출
        log.info("Member {} removed bookmark from board {}", member.getId(), board.getId());
    }

    /**
     * 특정 게시글의 총 북마크 수를 조회합니다.
     *
     * @param boardId 조회할 게시글의 ID
     * @return 해당 게시글의 총 북마크 수
     */
    @Override
    @Transactional(readOnly = true)
    public int getBookmarkCount(Long boardId) {
        Board board = findBoardById(boardId);
        return board.getBookmarkCount();
    }

    /**
     * 특정 회원이 해당 게시글을 북마크했는지 여부를 확인합니다.
     *
     * @param boardId     확인할 게시글의 ID
     * @param memberEmail 확인할 회원의 이메일
     * @return 북마크했다면 true, 그렇지 않으면 false
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarkedByUser(Long boardId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);
        return bookmarkRepository.existsByMemberAndBoard(member, board);
    }

    //== Private Helper Methods ==//

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }
}