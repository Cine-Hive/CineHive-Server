package com.example.CineHive.service.post;

import com.example.CineHive.domain.post.bookmark.BookmarkServiceImpl;
import com.example.CineHive.domain.post.bookmark.Bookmark;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.bookmark.BookmarkRepository;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.UserRepository;
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
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    private User testUser;
    private Post testPost;
    private final Long testPostId = 1L;
    private final String testUserEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder().email(testUserEmail).build();
        User postAuthor = User.builder().email("author@example.com").build();

        testPost = Post.builder()
                .title("테스트 게시글")
                .content("테스트 내용입니다.")
                .user(postAuthor)
                .build();

        ReflectionTestUtils.setField(testPost, "id", testPostId);
    }

    @Nested
    @DisplayName("북마크 추가 (addBookmark)")
    class AddBookmark {

        @Test
        @DisplayName("✅ 성공: 북마크를 성공적으로 추가한다.")
        void addBookmark_success() {
            // given
            given(userRepository.findByEmail(testUserEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testPostId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByUserAndPost(testUser, testPost)).willReturn(false);

            // when
            bookmarkService.addBookmark(testPostId, testUserEmail);

            // then
            verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
            verify(testPost, times(1)).increaseBookmarkCount();
        }

        @Test
        @DisplayName("❌ 실패: 이미 북마크한 경우 BusinessException을 던진다.")
        void addBookmark_fail_alreadyExists() {
            // given
            given(userRepository.findByEmail(testUserEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testPostId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByUserAndPost(testUser, testPost)).willReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    bookmarkService.addBookmark(testPostId, testUserEmail)
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
            testBookmark = Bookmark.builder().user(testUser).post(testPost).build();
        }

        @Test
        @DisplayName("✅ 성공: 북마크를 성공적으로 제거한다.")
        void removeBookmark_success() {
            // given
            given(userRepository.findByEmail(testUserEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testPostId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.findByUserAndPost(testUser, testPost)).willReturn(Optional.of(testBookmark));

            // when
            bookmarkService.removeBookmark(testPostId, testUserEmail);

            // then
            verify(bookmarkRepository, times(1)).delete(testBookmark);
            verify(testPost, times(1)).decreaseBookmarkCount();
        }

        @Test
        @DisplayName("❌ 실패: 북마크가 존재하지 않는 경우 BusinessException을 던진다.")
        void removeBookmark_fail_notFound() {
            // given
            given(userRepository.findByEmail(testUserEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testPostId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.findByUserAndPost(testUser, testPost)).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    bookmarkService.removeBookmark(testPostId, testUserEmail)
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
            given(userRepository.findByEmail(testUserEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testPostId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByUserAndPost(testUser, testPost)).willReturn(true);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(testPostId, testUserEmail);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("✅ 성공: 북마크가 존재하지 않을 때 false를 반환한다.")
        void isBookmarkedByUser_returnsFalse_whenNotExists() {
            // given
            given(userRepository.findByEmail(testUserEmail)).willReturn(Optional.of(testUser));
            given(postRepository.findById(testPostId)).willReturn(Optional.of(testPost));
            given(bookmarkRepository.existsByUserAndPost(testUser, testPost)).willReturn(false);

            // when
            boolean result = bookmarkService.isBookmarkedByUser(testPostId, testUserEmail);

            // then
            assertThat(result).isFalse();
        }
    }
}