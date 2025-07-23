package com.example.CineHive.service.post;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.Bookmark;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.post.PostRepository;
import com.example.CineHive.repository.post.BookmarkRepository;
import com.example.CineHive.repository.user.UserRepository;
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
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    private User testUser;
    private Post testPost;
    private final Long testBoardId = 1L;
    private final String testMemberEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // 테스트용 사용자(북마크 행위자) 생성
        testUser = User.builder()
                .email(testMemberEmail)
                .build();

        // 테스트용 게시글 작성자 생성
        User boardAuthor = User.builder()
                .email("author@example.com")
                .build();

        // 테스트용 게시글 생성 (빌더 사용)
        testPost = Post.builder()
                .brdTitle("테스트 게시글")
                .brdContent("테스트 내용입니다.")
                .member(boardAuthor)
                .build();

        // ReflectionTestUtils를 사용하여 엔티티의 ID를 테스트용으로 설정
        ReflectionTestUtils.setField(testPost, "id", testBoardId);
    }

    @Nested
    @DisplayName("북마크 추가 (addBookmark)")
    class AddBookmark {

        @Test
        @DisplayName("✅ 성공: 북마크를 성공적으로 추가한다.")
        void addBookmark_success() {
            // given
            given(userRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testBoardId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByMemberAndBoard(testUser, testPost)).willReturn(false);

            // when
            bookmarkService.addBookmark(testBoardId, testMemberEmail);

            // then
            verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
        }

        @Test
        @DisplayName("❌ 실패: 이미 북마크한 경우 BusinessException(BOOKMARK_ALREADY_EXISTS)을 던진다.")
        void addBookmark_fail_alreadyExists() {
            // given
            given(userRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testBoardId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByMemberAndBoard(testUser, testPost)).willReturn(true);

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
            testBookmark = Bookmark.builder().member(testUser).board(testPost).build();
        }

        @Test
        @DisplayName("✅ 성공: 북마크를 성공적으로 제거한다.")
        void removeBookmark_success() {
            // given
            given(userRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testBoardId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.findByMemberAndBoard(testUser, testPost)).willReturn(Optional.of(testBookmark));

            // when
            bookmarkService.removeBookmark(testBoardId, testMemberEmail);

            // then
            verify(bookmarkRepository, times(1)).delete(testBookmark);
        }

        @Test
        @DisplayName("❌ 실패: 북마크가 존재하지 않는 경우 BusinessException(BOOKMARK_NOT_FOUND)을 던진다.")
        void removeBookmark_fail_notFound() {
            // given
            given(userRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testBoardId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.findByMemberAndBoard(testUser, testPost)).willReturn(Optional.empty());

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
            given(userRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testBoardId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByMemberAndBoard(testUser, testPost)).willReturn(true);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(testBoardId, testMemberEmail);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("✅ 성공: 북마크가 존재하지 않을 때 false를 반환한다.")
        void isBookmarkedByUser_returnsFalse_whenNotExists() {
            // given
            given(userRepository.findByEmail(testMemberEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testBoardId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByMemberAndBoard(testUser, testPost)).willReturn(false);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(testBoardId, testMemberEmail);

            // then
            assertThat(result).isFalse();
        }
    }
}
