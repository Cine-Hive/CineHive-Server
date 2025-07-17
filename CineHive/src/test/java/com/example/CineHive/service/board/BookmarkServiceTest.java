package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

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
    private final Long testBoardId = 1L;
    private final String testMemberEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // 테스트용 사용자(북마크 행위자) 생성
        testMember = Member.builder()
                .email(testMemberEmail)
                .build();

        // 테스트용 게시글 작성자 생성
        Member boardAuthor = Member.builder()
                .email("author@example.com")
                .build();

        // 테스트용 게시글 생성 (빌더 사용)
        testBoard = Board.builder()
                .brdTitle("테스트 게시글")
                .brdContent("테스트 내용입니다.")
                .member(boardAuthor)
                .build();

        // ReflectionTestUtils를 사용하여 엔티티의 ID를 테스트용으로 설정
        ReflectionTestUtils.setField(testBoard, "id", testBoardId);
    }

    @Nested
    @DisplayName("북마크 추가 (addBookmark)")
    class AddBookmark {

        @Test
        @DisplayName("✅ 성공: 북마크를 성공적으로 추가한다.")
        void addBookmark_success() {
            // given
            given(memberRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testMember));
            given(boardRepository.findById(testBoardId)).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(false);

            // when
            bookmarkService.addBookmark(testBoardId, testMemberEmail);

            // then
            verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("❌ 실패: 이미 북마크한 경우 BusinessException(BOOKMARK_ALREADY_EXISTS)을 던진다.")
        void addBookmark_fail_alreadyExists() {
            // given
            given(memberRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testMember));
            given(boardRepository.findById(testBoardId)).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    bookmarkService.addBookmark(testBoardId, testMemberEmail)
            );

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOKMARK_ALREADY_EXISTS);
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
            given(memberRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testMember));
            given(boardRepository.findById(testBoardId)).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.findByMemberAndBoard(testMember, testBoard)).willReturn(Optional.of(testBookmark));

            // when
            bookmarkService.removeBookmark(testBoardId, testMemberEmail);

            // then
            verify(bookmarkRepository, times(1)).delete(testBookmark);
        }

        @Test
        @DisplayName("❌ 실패: 북마크가 존재하지 않는 경우 BusinessException(BOOKMARK_NOT_FOUND)을 던진다.")
        void removeBookmark_fail_notFound() {
            // given
            given(memberRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testMember));
            given(boardRepository.findById(testBoardId)).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.findByMemberAndBoard(testMember, testBoard)).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    bookmarkService.removeBookmark(testBoardId, testMemberEmail)
            );

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.BOOKMARK_NOT_FOUND);
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
            given(memberRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testMember));
            given(boardRepository.findById(testBoardId)).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(true);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(testBoardId, testMemberEmail);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("✅ 성공: 북마크가 존재하지 않을 때 false를 반환한다.")
        void isBookmarkedByUser_returnsFalse_whenNotExists() {
            // given
            given(memberRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testMember));
            given(boardRepository.findById(testBoardId)).willReturn(Optional.of(testBoard));
            given(bookmarkRepository.existsByMemberAndBoard(testMember, testBoard)).willReturn(false);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(testBoardId, testMemberEmail);

            // then
            assertThat(result).isFalse();
        }
    }
}
