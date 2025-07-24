package com.example.CineHive.controller.post;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.user.Gender;
import com.example.CineHive.entity.user.ProviderType;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.post.PostRepository;
import com.example.CineHive.repository.user.UserRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("PostController 통합 테스트")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        postRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        testUser = createAndSaveUser("test@example.com", "테스트유저");
        otherUser = createAndSaveUser("other@example.com", "다른유저");
    }

    private User createAndSaveUser(String email, String nickname) {
        return userRepository.save(User.builder().email(email).password("password").name(nickname).nickname(nickname)
                .gender(Gender.MALE).provider(ProviderType.LOCAL).build());
    }

    @Nested
    @DisplayName("게시글 생성 (POST /api/v1/posts)")
    class CreatePost {
        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com")
        class Success {
            @Test
            @DisplayName("✅ 유효한 데이터로 요청 시, 게시글이 생성되고 201 응답을 반환한다.")
            void createPost_success() throws Exception {
                // given
                Map<String, String> request = Map.of("title", "새 제목", "content", "새 내용");

                // when & then
                mockMvc.perform(post("/api/v1/posts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.title").value("새 제목"))
                        .andExpect(jsonPath("$.data.userNickname").value(testUser.getNickname()));
            }
        }

        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            @ParameterizedTest
            @ValueSource(strings = {"", " "})
            @WithMockUser(username = "test@example.com")
            @DisplayName("❌ 제목 또는 내용이 비어있으면 400 Bad Request를 반환한다.")
            void createPost_withBlankField_shouldFail(String blankValue) throws Exception {
                // given
                Map<String, String> requestWithBlankTitle = Map.of("title", blankValue, "content", "내용 있음");
                Map<String, String> requestWithBlankContent = Map.of("title", "제목 있음", "content", blankValue);

                // when & then
                mockMvc.perform(post("/api/v1/posts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestWithBlankTitle)))
                        .andExpect(status().isBadRequest());
                mockMvc.perform(post("/api/v1/posts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestWithBlankContent)))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @WithAnonymousUser
            @DisplayName("❌ 인증되지 않은 사용자가 요청하면 401 Unauthorized를 반환한다.")
            void createPost_withAnonymousUser_shouldFail() throws Exception {
                // given
                Map<String, String> request = Map.of("title", "제목", "content", "내용");

                // when & then
                mockMvc.perform(post("/api/v1/posts").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("게시글 조회")
    class ReadPost {
        @Nested
        @DisplayName("상세 조회")
        class GetSingle {
            @Test
            @DisplayName("✅ 성공: 게시글 ID로 상세 조회 시 200 응답과 함께 조회수가 1 증가한다.")
            void getPostById_success() throws Exception {
                // given
                Post post = postRepository.save(Post.builder().user(testUser).title("조회용").content("내용").build());
                int initialViews = post.getViews();

                // when & then
                mockMvc.perform(get("/api/v1/posts/{id}", post.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.id").value(post.getId()))
                        .andExpect(jsonPath("$.data.views").value(initialViews + 1));
            }

            @Test
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 조회 시 404 Not Found를 반환한다.")
            void getPostById_fail_notFound() throws Exception {
                // when & then
                mockMvc.perform(get("/api/v1/posts/99999"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message").value(ErrorCode.POST_NOT_FOUND.getMessage()));
            }
        }

        @Nested
        @DisplayName("목록 조회")
        class GetList {
            @BeforeEach
            void setUpList() {
                IntStream.range(0, 20).forEach(i -> postRepository.save(Post.builder().user(otherUser).title("목록 " + i).content("내용").build()));
            }

            @Test
            @DisplayName("✅ 성공: 페이징 파라미터가 정상 동작하고 메타데이터를 정확히 반환한다.")
            void getPostList_withPaging() throws Exception {
                // when & then
                mockMvc.perform(get("/api/v1/posts").param("page", "2").param("size", "5"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.content.length()").value(5))
                        .andExpect(jsonPath("$.data.page").value(1)) // API는 0-based index
                        .andExpect(jsonPath("$.data.size").value(5))
                        .andExpect(jsonPath("$.data.totalElements").value(20))
                        .andExpect(jsonPath("$.data.totalPages").value(4))
                        .andExpect(jsonPath("$.data.last").value(false));
            }

            @Test
            @DisplayName("✅ 성공: 정렬(sort) 파라미터가 정상 동작한다.")
            void getPostList_withSort() throws Exception {
                // given
                Post mostViewedPost = Post.builder().user(testUser).title("조회수 1등").content("인기글").build();
                IntStream.range(0, 5).forEach(i -> mostViewedPost.increaseViews()); // 조회수 증가
                postRepository.save(mostViewedPost);

                // when & then
                mockMvc.perform(get("/api/v1/posts").param("sort", "VIEWS"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.content[0].title").value("조회수 1등"));
            }
        }
    }

    @Nested
    @DisplayName("게시글 수정 (PUT /api/v1/posts/{id})")
    class UpdatePost {
        private Post testPost;
        @BeforeEach
        void setUp() {
            testPost = postRepository.save(Post.builder().user(testUser).title("원본").content("원본").build());
        }

        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com")
        class Success {
            @Test
            @DisplayName("✅ 성공: 게시글 작성자가 수정 요청 시 200 응답과 함께 내용이 변경된다.")
            void updatePost_success() throws Exception {
                // given
                Map<String, String> request = Map.of("title", "수정된 제목", "content", "수정된 내용");

                // when & then
                mockMvc.perform(put("/api/v1/posts/{id}", testPost.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.title").value("수정된 제목"));
            }
        }

        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            @Test
            @WithMockUser(username = "other@example.com")
            @DisplayName("❌ 실패: 권한 없는 사용자가 요청하면 403 Forbidden을 반환한다.")
            void updatePost_withOtherUser_shouldFail() throws Exception {
                Map<String, String> request = Map.of("title", "수정 시도", "content", "권한 없음");
                mockMvc.perform(put("/api/v1/posts/{id}", testPost.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isForbidden());
            }

            @Test
            @WithMockUser(username = "test@example.com")
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 요청하면 404 Not Found를 반환한다.")
            void updatePost_withNonExistentId_shouldFail() throws Exception {
                Map<String, String> request = Map.of("title", "수정", "content", "수정");
                mockMvc.perform(put("/api/v1/posts/9999").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message").value(ErrorCode.POST_NOT_FOUND.getMessage()));
            }
        }
    }

    @Nested
    @DisplayName("게시글 삭제 (DELETE /api/v1/posts/{id})")
    class DeletePost {
        private Post testPost;
        @BeforeEach
        void setUp() {
            testPost = postRepository.save(Post.builder().user(testUser).title("삭제용").content("삭제용").build());
        }

        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com")
        class Success {
            @Test
            @DisplayName("✅ 성공: 게시글 작성자가 삭제 요청 시 200 응답과 함께 게시글이 삭제된다.")
            void deletePost_success() throws Exception {
                // when & then
                mockMvc.perform(delete("/api/v1/posts/{id}", testPost.getId()).with(csrf()))
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
            void deletePost_withOtherUser_shouldFail() throws Exception {
                mockMvc.perform(delete("/api/v1/posts/{id}", testPost.getId()).with(csrf()))
                        .andExpect(status().isForbidden());
            }
        }
    }
}