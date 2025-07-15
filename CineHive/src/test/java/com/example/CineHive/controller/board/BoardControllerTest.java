package com.example.CineHive.controller.board;

import com.example.CineHive.dto.board.BoardSortType;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.member.*;
import com.example.CineHive.repository.board.BoardRepository;
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
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("BoardController 통합 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;

    private Member testMember;
    private Member otherMember;

    @BeforeEach
    void setUp() {
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        testMember = createAndSaveMember("test@example.com", "테스트유저");
        otherMember = createAndSaveMember("other@example.com", "다른유저");
    }

    private Member createAndSaveMember(String email, String nickname) {
        return memberRepository.save(Member.builder().email(email).password("password").name(nickname).nickname(nickname)
                .gender(Gender.MALE).provider(ProviderType.LOCAL).role(MemberRole.ROLE_USER).build());
    }

    // --- 1. 생성(Create) 테스트 ---
    @Nested
    @DisplayName("게시글 생성 (POST /api/v1/boards)")
    class CreateBoard {
        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com", roles = "USER")
        class Success {
            @Test
            @DisplayName("✅ 유효한 데이터로 요청 시, 게시글이 생성되고 201 응답을 반환한다.")
            void createBoard_success() throws Exception {
                // given
                Map<String, String> request = Map.of("brdTitle", "새 제목", "brdContent", "새 내용");

                // when & then
                mockMvc.perform(post("/api/v1/boards").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.brdTitle").value("새 제목"))
                        .andExpect(jsonPath("$.data.memNickname").value(testMember.getNickname()));
            }
        }

        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            @ParameterizedTest
            @ValueSource(strings = {"", " "})
            @WithMockUser(username = "test@example.com", roles = "USER")
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

    // --- 2. 조회(Read) 테스트 ---
    @Nested
    @DisplayName("게시글 조회 (GET /api/v1/boards, GET /api/v1/boards/{id})")
    class ReadBoard {

        @Nested
        @DisplayName("상세 조회")
        class GetSingle {
            @Test
            @DisplayName("✅ 성공: 게시글 ID로 상세 조회 시 200 응답과 함께 조회수가 1 증가한다.")
            void getBoardById_success() throws Exception {
                // given
                Board board = boardRepository.save(Board.builder().member(testMember).brdTitle("조회용").brdContent("내용").build());
                int initialViews = board.getViews();

                // when & then
                mockMvc.perform(get("/api/v1/boards/{id}", board.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.id").value(board.getId()))
                        .andExpect(jsonPath("$.data.views").value(initialViews + 1));
            }

            @Test
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 조회 시 404 Not Found를 반환한다.")
            void getBoardById_fail_notFound() throws Exception {
                // when & then
                mockMvc.perform(get("/api/v1/boards/99999"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message").value("해당 ID의 게시글을 찾을 수 없습니다: 99999"));
            }
        }

        @Nested
        @DisplayName("목록 조회")
        class GetList {
            @BeforeEach
            void setUpList() {
                IntStream.range(0, 20).forEach(i -> boardRepository.save(Board.builder().member(otherMember).brdTitle("목록 " + i).brdContent("내용").build()));
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
                Board mostViewedBoard = boardRepository.save(Board.builder().member(testMember).brdTitle("조회수 1등").brdContent("인기글").build());
                mostViewedBoard.increaseViews();
                boardRepository.save(mostViewedBoard);

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

    // --- 3. 수정(Update) 테스트 ---
    @Nested
    @DisplayName("게시글 수정 (PUT /api/v1/boards/{id})")
    class UpdateBoard {
        private Board testBoard;
        @BeforeEach
        void setUp() {
            testBoard = boardRepository.save(Board.builder().member(testMember).brdTitle("원본").brdContent("원본").build());
        }

        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com", roles = "USER")
        class Success {
            @Test
            @DisplayName("✅ 성공: 게시글 작성자가 수정 요청 시 200 응답과 함께 내용이 변경된다.")
            void updateBoard_success() throws Exception {
                // given
                Map<String, String> request = Map.of("brdTitle", "수정된 제목", "brdContent", "수정된 내용");

                // when & then
                mockMvc.perform(put("/api/v1/boards/{id}", testBoard.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
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
                mockMvc.perform(put("/api/v1/boards/{id}", testBoard.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @WithMockUser(username = "other@example.com", roles = "USER")
            @DisplayName("❌ 실패: 권한 없는 사용자가 요청하면 403 Forbidden을 반환한다.")
            void updateBoard_withOtherUser_shouldFail() throws Exception {
                Map<String, String> request = Map.of("brdTitle", "수정 시도", "brdContent", "권한 없음");
                mockMvc.perform(put("/api/v1/boards/{id}", testBoard.getId()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isForbidden());
            }

            @Test
            @WithMockUser(username = "test@example.com", roles = "USER")
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 요청하면 404 Not Found를 반환한다.")
            void updateBoard_withNonExistentId_shouldFail() throws Exception {
                Map<String, String> request = Map.of("brdTitle", "수정", "brdContent", "수정");
                mockMvc.perform(put("/api/v1/boards/9999").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message").value("해당 ID의 게시글을 찾을 수 없습니다: 9999"));
            }
        }
    }

    // --- 4. 삭제(Delete) 테스트 ---
    @Nested
    @DisplayName("게시글 삭제 (DELETE /api/v1/boards/{id})")
    class DeleteBoard {
        private Board testBoard;
        @BeforeEach
        void setUp() {
            testBoard = boardRepository.save(Board.builder().member(testMember).brdTitle("삭제용").brdContent("삭제용").build());
        }

        @Nested
        @DisplayName("성공 시나리오")
        @WithMockUser(username = "test@example.com", roles = "USER")
        class Success {
            @Test
            @DisplayName("✅ 성공: 게시글 작성자가 삭제 요청 시 200 응답과 함께 게시글이 삭제된다.")
            void deleteBoard_success() throws Exception {
                // when & then
                mockMvc.perform(delete("/api/v1/boards/{id}", testBoard.getId()).with(csrf()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.message").value("게시글이 성공적으로 삭제되었습니다."));

                assertThat(boardRepository.findById(testBoard.getId())).isEmpty();
            }
        }

        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            @Test
            @WithMockUser(username = "other@example.com", roles = "USER")
            @DisplayName("❌ 실패: 권한 없는 사용자가 요청하면 403 Forbidden을 반환한다.")
            void deleteBoard_withOtherUser_shouldFail() throws Exception {
                mockMvc.perform(delete("/api/v1/boards/{id}", testBoard.getId()).with(csrf()))
                        .andExpect(status().isForbidden());
            }

            @Test
            @WithAnonymousUser
            @DisplayName("❌ 실패: 인증되지 않은 사용자가 요청하면 401 Unauthorized를 반환한다.")
            void deleteBoard_withAnonymousUser_shouldFail() throws Exception {
                mockMvc.perform(delete("/api/v1/boards/{id}", testBoard.getId()).with(csrf()))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @WithMockUser(username = "test@example.com", roles = "USER")
            @DisplayName("❌ 실패: 존재하지 않는 게시글 ID로 요청하면 404 Not Found를 반환한다.")
            void deleteBoard_withNonExistentId_shouldFail() throws Exception {
                mockMvc.perform(delete("/api/v1/boards/9999").with(csrf()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error.message", containsString("해당 ID의 게시글을 찾을 수 없습니다")));
            }
        }
    }
}