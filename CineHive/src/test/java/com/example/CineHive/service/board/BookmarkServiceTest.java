package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BookmarkAlreadyExistsException;
import com.example.CineHive.exception.BookmarkNotFoundException;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.BookmarkRepository;
import com.example.CineHive.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkService 테스트")
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    private Member testMember;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .build();
        testBoard = Board.builder()
                .member(testMember)
                .build();
    }

    @Nested
    @DisplayName("북마크 추가 (addBookmark)")
    class AddBookmark {

        @Test
        @DisplayName("✅ 성공: 북마크를 성공적으로 추가한다.")
        void addBookmark_success() {
            // given
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(testMember));
            given(boardRepository.findById(anyLong())).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(false);

            bookmarkService.addBookmark(10L, testMember.getEmail());

            // then
            verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
            // board의 increaseBookmarkCount() 호출 검증은 이 테스트의 주 목적이 아니며,
            // 통합 테스트에서 실제 값이 증가하는 것으로 검증하는 것이 더 효과적입니다.
        }

        @Test
        @DisplayName("❌ 실패: 이미 북마크한 경우 BookmarkAlreadyExistsException을 던진다.")
        void addBookmark_fail_alreadyExists() {
            // given
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(testMember));
            given(boardRepository.findById(anyLong())).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(true);

            // when & then
            assertThrows(BookmarkAlreadyExistsException.class, () -> {
                bookmarkService.addBookmark(10L, testMember.getEmail());
            });

            verify(bookmarkRepository, never()).save(any(Bookmark.class));
        }
    }

    @Nested
    @DisplayName("북마크 제거 (removeBookmark)")
    class RemoveBookmark {
        private Bookmark testBookmark;

        @BeforeEach
        void setUp() {
            testBookmark = Bookmark.builder().member(testMember).board(testBoard).build();
        }

        @Test
        @DisplayName("✅ 성공: 북마크를 성공적으로 제거한다.")
        void removeBookmark_success() {
            // given
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(testMember));
            given(boardRepository.findById(anyLong())).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.findByMemberAndBoard(testMember, testBoard)).willReturn(Optional.of(testBookmark));

            // when
            bookmarkService.removeBookmark(10L, testMember.getEmail());

            // then
            verify(bookmarkRepository, times(1)).delete(testBookmark);
        }

        @Test
        @DisplayName("❌ 실패: 북마크가 존재하지 않는 경우 BookmarkNotFoundException을 던진다.")
        void removeBookmark_fail_notFound() {
            // given
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(testMember));
            given(boardRepository.findById(anyLong())).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.findByMemberAndBoard(testMember, testBoard)).willReturn(Optional.empty());

            // when & then
            assertThrows(BookmarkNotFoundException.class, () -> {
                bookmarkService.removeBookmark(10L, testMember.getEmail());
            });

            verify(bookmarkRepository, never()).delete(any(Bookmark.class));
        }
    }

    @Nested
    @DisplayName("북마크 상태 조회 (isBookmarkedByUser)")
    class IsBookmarkedByUser {

        @Test
        @DisplayName("✅ 성공: 북마크가 존재할 때 true를 반환한다.")
        void isBookmarkedByUser_returnsTrue_whenExists() {
            // given
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(testMember));
            given(boardRepository.findById(anyLong())).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(true);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(10L, testMember.getEmail());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("✅ 성공: 북마크가 존재하지 않을 때 false를 반환한다.")
        void isBookmarkedByUser_returnsFalse_whenNotExists() {
            // given
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(testMember));
            given(boardRepository.findById(anyLong())).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(false);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(10L, testMember.getEmail());

            // then
            assertThat(result).isFalse();
        }
    }
}