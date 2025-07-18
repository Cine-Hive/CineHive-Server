package com.example.CineHive.controller.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.entity.member.*;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.BookmarkRepository;
import com.example.CineHive.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("BookmarkController 통합 테스트")
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;

    private Member boardOwner;
    private Member bookmarker;
    private Member anotherUser;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        bookmarkRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        boardOwner = createMember("owner@example.com", "boardOwner");
        bookmarker = createMember("bookmarker@example.com", "bookmarker");
        anotherUser = createMember("another@example.com", "anotherUser");

        testBoard = boardRepository.save(Board.builder()
                .brdTitle("테스트 게시글")
                .brdContent("내용")
                .member(boardOwner)
                .build());
    }

    private Member createMember(String email, String nickname) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("password")
                .name(nickname)
                .nickname(nickname)
                .gender(Gender.MALE)
                .provider(ProviderType.LOCAL)
                .role(MemberRole.ROLE_USER)
                .build());
    }

    @Nested
    @DisplayName("북마크 추가 테스트")
    @WithMockUser(username = "bookmarker@example.com", roles = "USER")
    class AddBookmark {

        @Test
        @DisplayName("✅ 성공: 사용자가 게시글을 성공적으로 북마크한다.")
        void addBookmark_success() throws Exception {
            mockMvc.perform(post("/api/v1/boards/{boardId}/bookmarks", testBoard.getId())
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("게시글을 북마크했습니다."));

            Board boardAfterBookmark = boardRepository.findById(testBoard.getId()).get();
            assertThat(boardAfterBookmark.getBookmarkCount()).isEqualTo(1);
            assertThat(bookmarkRepository.existsByMemberAndBoard(bookmarker, testBoard)).isTrue();
        }

        @Test
        @DisplayName("❌ 실패(409): 이미 북마크한 게시글을 다시 북마크하면 409 Conflict를 반환한다.")
        void addBookmark_fail_alreadyExists() throws Exception {
            bookmarkRepository.save(new Bookmark(bookmarker, testBoard));
            testBoard.increaseBookmarkCount();
            boardRepository.saveAndFlush(testBoard);

            mockMvc.perform(post("/api/v1/boards/{boardId}/bookmarks", testBoard.getId())
                            .with(csrf()))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("북마크 취소 테스트 (북마크한 사용자로)")
    @WithMockUser(username = "bookmarker@example.com", roles = "USER")
    class RemoveBookmark_Success {

        @BeforeEach
        void setUp() {
            bookmarkRepository.save(new Bookmark(bookmarker, testBoard));
            testBoard.increaseBookmarkCount();
            boardRepository.saveAndFlush(testBoard);
        }

        @Test
        @DisplayName("✅ 성공: 사용자가 북마크한 게시글을 성공적으로 취소한다.")
        void removeBookmark_success() throws Exception {
            mockMvc.perform(delete("/api/v1/boards/{boardId}/bookmarks", testBoard.getId())
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("북마크를 취소했습니다."));

            Board boardAfterRemove = boardRepository.findById(testBoard.getId()).get();
            assertThat(boardAfterRemove.getBookmarkCount()).isEqualTo(0);
            assertThat(bookmarkRepository.existsByMemberAndBoard(bookmarker, testBoard)).isFalse();
        }
    }

    @Nested
    @DisplayName("북마크 취소 테스트 (북마크하지 않은 사용자로)")
    @WithMockUser(username = "another@example.com", roles = "USER")
    class RemoveBookmark_Fail {

        @BeforeEach
        void setUp() {
            bookmarkRepository.save(new Bookmark(bookmarker, testBoard));
            testBoard.increaseBookmarkCount();
            boardRepository.saveAndFlush(testBoard);
        }

        @Test
        @DisplayName("❌ 실패(404): 북마크하지 않은 게시글의 북마크를 취소하면 404 Not Found를 반환한다.")
        void removeBookmark_fail_notFound() throws Exception {
            mockMvc.perform(delete("/api/v1/boards/{boardId}/bookmarks", testBoard.getId())
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("북마크 상태 및 개수 조회 테스트")
    class GetBookmarkStatus {

        @Test
        @DisplayName("✅ 성공: 북마크 개수를 정확히 반환한다.")
        void getBookmarkCount_success() throws Exception {
            bookmarkRepository.save(new Bookmark(bookmarker, testBoard));
            testBoard.increaseBookmarkCount();
            boardRepository.saveAndFlush(testBoard);

            mockMvc.perform(get("/api/v1/boards/{boardId}/bookmarks/count", testBoard.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.bookmarkCount").value(1));
        }

        @Test
        @WithMockUser(username = "bookmarker@example.com", roles = "USER")
        @DisplayName("✅ 성공: 사용자가 북마크한 경우 isBookmarked가 true로 반환된다.")
        void getBookmarkStatus_success_whenBookmarked() throws Exception {
            bookmarkRepository.save(new Bookmark(bookmarker, testBoard));

            mockMvc.perform(get("/api/v1/boards/{boardId}/bookmarks/status", testBoard.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isBookmarked").value(true));
        }

        @Test
        @WithMockUser(username = "bookmarker@example.com", roles = "USER")
        @DisplayName("✅ 성공: 사용자가 북마크하지 않은 경우 isBookmarked가 false로 반환된다.")
        void getBookmarkStatus_success_whenNotBookmarked() throws Exception {
            mockMvc.perform(get("/api/v1/boards/{boardId}/bookmarks/status", testBoard.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isBookmarked").value(false));
        }

        @Test
        @DisplayName("✅ 성공: 인증되지 않은 사용자가 상태 조회를 시도하면 200 OK와 함께 false를 반환한다.")
        void getBookmarkStatus_forAnonymousUser() throws Exception {
            mockMvc.perform(get("/api/v1/boards/{boardId}/bookmarks/status", testBoard.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isBookmarked").value(false));
        }
    }
}