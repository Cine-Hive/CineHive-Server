package com.example.CineHive.controller.board;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.user.*;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.board.PostRepository;
import com.example.CineHive.repository.member.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BoardController의 API 엔드포인트에 대한 통합 테스트입니다.
 * 실제 데이터베이스와의 상호작용을 포함하여 컨트롤러의 동작을 검증합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("BoardController 통합 테스트")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        testUser = createAndSaveMember("test@example.com", "테스트유저");
        otherUser = createAndSaveMember("other@example.com", "다른유저");
    }

    private User createAndSaveMember(String email, String nickname) {
        return memberRepository.save(User.builder().email(email).password("password").name(nickname).nickname(nickname)
                .gender(Gender.MALE).provider(ProviderType.LOCAL).build());
    }

    /**
     * 게시글 생성(POST /api/v1/boards) API에 대한 테스트
     */
    @Nested
    @DisplayName("게시글 생성 (POST /api/v1/boards)")
    class CreatePost {
        /**
         * 게시글 생성 성공 시나리오
         */
        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com")
        class Success {
            /**
             * Given: 유효한 게시글 제목과 내용, 그리고 인증된 사용자 정보
             * When: 게시글 생성 API를 호출하면
             * Then: HTTP 201 Created 상태 코드와 함께 생성된 게시글 정보가 반환된다.
             */
            @Test
            @DisplayName("✅ 유효한 데이터로 요청 시, 게시글이 생성되고 201 응답을 반환한다.")
            void createBoard_success() throws Exception {
                // given
                Map<String, String> request = Map.of("brdTitle", "새 제목", "brdContent", "새 내용");

                // when & then
                mockMvc.perform(post("/api/v1/boards").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.brdTitle").value("새 제목"))
                        .andExpect(jsonPath("$.data.memNickname").value(testUser.getNickname()));
            }
        }

        /**
         * 게시글 생성 실패 시나리오
         */
        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            /**
             * Given: 제목 또는 내용이 비어있는 요청 데이터
             * When: 게시글 생성 API를 호출하면
             * Then: HTTP 400 Bad Request 상태 코드를 반환한다.
             */
            @ParameterizedTest
            @ValueSource(strings = {"", " "})
            @WithMockUser(username = "test@example.com")
            @DisplayName("❌ 제목 또는 내용이 비어있으면 400 Bad Request를 반환한다.")
            void createBoard_withBlankField_shouldFail(String blankValue) throws Exception {
                // given
                Map<String, String> requestWithBlankTitle = Map.of("brdTitle", blankValue, "brdContent", "내용 있음");
                Map<String, String> requestWithBlankContent = Map.of("brdTitle", "제목 있음", "brdContent", blankValue);

                // when & then
                mockMvc.perform(post("/api/v1/boards").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestWithBlankTitle)))
                        .andExpect(status().isBadRequest());
                mockMvc.perform(post("/api/v1/boards").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestWithBlankContent)))
                        .andExpect(status().isBadRequest());
            }

            /**
             * Given: 인증되지 않은 익명 사용자
             * When: 게시글 생성 API를 호출하면
             * Then: HTTP 401 Unauthorized 상태 코드를 반환한다.
             */
            @Test
            @WithAnonymousUser
            @DisplayName("❌ 인증되지 않은 사용자가 요청하면 401 Unauthorized를 반환한다.")
            void createBoard_withAnonymousUser_shouldFail() throws Exception {
                // given
                Map<String, String> request = Map.of("brdTitle", "제목", "brdContent", "내용");

                // when & then
                mockMvc.perform(post("/api/v1/boards").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    /**
     * 게시글 조회(GET /api/v1/boards, GET /api/v1/boards/{id}) API에 대한 테스트
     */
    @Nested
    @DisplayName("게시글 조회")
    class ReadPost {
        /**
         * 게시글 상세 조회 테스트
         */
        @Nested
        @DisplayName("상세 조회")
        class GetSingle {
            @Test
            @DisplayName("✅ 성공: 게시글 ID로 상세 조회 시 200 응답과 함께 조회수가 1 증가한다.")
            void getBoardById_success() throws Exception {
                // given
                Post post = postRepository.save(Post.builder().member(testUser).brdTitle("조회용").brdContent("내용").build());
                int initialViews = post.getViews();

                // when & then
                mockMvc.perform(get("/api/v1/boards/{id}", post.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.id").value(post.getId()))
                        .andExpect(jsonPath("$.data.views").value(initialViews + 1));
            }

            @Test
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 조회 시 404 Not Found를 반환한다.")
            void getBoardById_fail_notFound() throws Exception {
                // when & then
                mockMvc.perform(get("/api/v1/boards/99999"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message").value(ErrorCode.BOARD_NOT_FOUND.getMessage()));
            }
        }

        /**
         * 게시글 목록 조회 테스트
         */
        @Nested
        @DisplayName("목록 조회")
        class GetList {
            @BeforeEach
            void setUpList() {
                IntStream.range(0, 20).forEach(i -> postRepository.save(Post.builder().member(otherUser).brdTitle("목록 " + i).brdContent("내용").build()));
            }

            @Test
            @DisplayName("✅ 성공: 페이징 파라미터가 정상 동작하고 메타데이터를 정확히 반환한다.")
            void getBoardList_withPaging() throws Exception {
                // when & then
                mockMvc.perform(get("/api/v1/boards").param("page", "2").param("size", "5"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.content.length()").value(5))
                        .andExpect(jsonPath("$.data.page").value(2))
                        .andExpect(jsonPath("$.data.size").value(5))
                        .andExpect(jsonPath("$.data.totalElements").value(20))
                        .andExpect(jsonPath("$.data.totalPages").value(4))
                        .andExpect(jsonPath("$.data.last").value(false));
            }

            @Test
            @DisplayName("✅ 성공: 정렬(sort) 파라미터가 정상 동작한다.")
            void getBoardList_withSort() throws Exception {
                // given
                Post mostViewedPost = postRepository.save(Post.builder().member(testUser).brdTitle("조회수 1등").brdContent("인기글").build());
                mostViewedPost.increaseViews();
                postRepository.save(mostViewedPost);

                // when & then
                mockMvc.perform(get("/api/v1/boards").param("sort", "VIEWS"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.content[0].brdTitle").value("조회수 1등"));
            }

            @ParameterizedTest
            @ValueSource(ints = {0, -1})
            @DisplayName("❌ 실패: 잘못된 페이지/사이즈 파라미터(1 미만) 요청 시 400 Bad Request를 반환한다.")
            void getBoardList_withInvalidPagingParam_shouldFail(int invalidValue) throws Exception {
                // when & then
                mockMvc.perform(get("/api/v1/boards").param("page", String.valueOf(invalidValue)))
                        .andExpect(status().isBadRequest());
                mockMvc.perform(get("/api/v1/boards").param("size", String.valueOf(invalidValue)))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    /**
     * 게시글 수정(PUT /api/v1/boards/{id}) API에 대한 테스트
     */
    @Nested
    @DisplayName("게시글 수정 (PUT /api/v1/boards/{id})")
    class UpdatePost {
        private Post testPost;
        @BeforeEach
        void setUp() {
            testPost = postRepository.save(Post.builder().member(testUser).brdTitle("원본").brdContent("원본").build());
        }

        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com")
        class Success {
            @Test
            @DisplayName("✅ 성공: 게시글 작성자가 수정 요청 시 200 응답과 함께 내용이 변경된다.")
            void updateBoard_success() throws Exception {
                // given
                Map<String, String> request = Map.of("brdTitle", "수정된 제목", "brdContent", "수정된 내용");

                // when & then
                mockMvc.perform(put("/api/v1/boards/{id}", testPost.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.brdTitle").value("수정된 제목"));
            }
        }

        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            @Test
            @WithAnonymousUser
            @DisplayName("❌ 실패: 인증되지 않은 사용자가 요청하면 401 Unauthorized를 반환한다.")
            void updateBoard_withAnonymousUser_shouldFail() throws Exception {
                Map<String, String> request = Map.of("brdTitle", "수정", "brdContent", "수정");
                mockMvc.perform(put("/api/v1/boards/{id}", testPost.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @WithMockUser(username = "other@example.com")
            @DisplayName("❌ 실패: 권한 없는 사용자가 요청하면 403 Forbidden을 반환한다.")
            void updateBoard_withOtherUser_shouldFail() throws Exception {
                Map<String, String> request = Map.of("brdTitle", "수정 시도", "brdContent", "권한 없음");
                mockMvc.perform(put("/api/v1/boards/{id}", testPost.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isForbidden());
            }

            @Test
            @WithMockUser(username = "test@example.com")
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 요청하면 404 Not Found를 반환한다.")
            void updateBoard_withNonExistentId_shouldFail() throws Exception {
                Map<String, String> request = Map.of("brdTitle", "수정", "brdContent", "수정");
                mockMvc.perform(put("/api/v1/boards/9999").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message").value(ErrorCode.BOARD_NOT_FOUND.getMessage()));
            }
        }
    }

    /**
     * 게시글 삭제(DELETE /api/v1/boards/{id}) API에 대한 테스트
     */
    @Nested
    @DisplayName("게시글 삭제 (DELETE /api/v1/boards/{id})")
    class DeletePost {
        private Post testPost;
        @BeforeEach
        void setUp() {
            testPost = postRepository.save(Post.builder().member(testUser).brdTitle("삭제용").brdContent("삭제용").build());
        }

        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com")
        class Success {
            @Test
            @DisplayName("✅ 성공: 게시글 작성자가 삭제 요청 시 200 응답과 함께 게시글이 삭제된다.")
            void deleteBoard_success() throws Exception {
                // when & then
                mockMvc.perform(delete("/api/v1/boards/{id}", testPost.getId()).with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.message").value("게시글이 성공적으로 삭제되었습니다."));

                assertThat(postRepository.findById(testPost.getId())).isEmpty();
            }
        }

        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            @Test
            @WithMockUser(username = "other@example.com")
            @DisplayName("❌ 실패: 권한 없는 사용자가 요청하면 403 Forbidden을 반환한다.")
            void deleteBoard_withOtherUser_shouldFail() throws Exception {
                mockMvc.perform(delete("/api/v1/boards/{id}", testPost.getId()).with(csrf()))
                        .andExpect(status().isForbidden());
            }

            @Test
            @WithAnonymousUser
            @DisplayName("❌ 실패: 인증되지 않은 사용자가 요청하면 401 Unauthorized를 반환한다.")
            void deleteBoard_withAnonymousUser_shouldFail() throws Exception {
                mockMvc.perform(delete("/api/v1/boards/{id}", testPost.getId()).with(csrf()))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @WithMockUser(username = "test@example.com")
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 요청하면 404 Not Found를 반환한다.")
            void deleteBoard_withNonExistentId_shouldFail() throws Exception {
                mockMvc.perform(delete("/api/v1/boards/9999").with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message").value(ErrorCode.BOARD_NOT_FOUND.getMessage()));
            }
        }
    }
}
