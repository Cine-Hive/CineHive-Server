package com.example.CineHive.controller.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.member.*;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.member.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    void setUp() {
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        testMember = memberRepository.save(Member.builder()
                .email("testuser@example.com")
                .password("password123")
                .name("테스트유저")
                .nickname("testUser")
                .gender(Gender.MALE)
                .provider(ProviderType.LOCAL)
                .role(MemberRole.ROLE_USER)
                .build());

        Member otherMember = memberRepository.save(Member.builder()
                .email("otheruser@example.com")
                .password("password123")
                .name("다른유저")
                .nickname("otherUser")
                .gender(Gender.FEMALE)
                .provider(ProviderType.LOCAL)
                .role(MemberRole.ROLE_USER)
                .build());
    }

    @Nested
    @DisplayName("게시글 생성 테스트")
    class CreateBoard {

        @Test
        @WithMockUser(username = "testuser@example.com", roles = "USER")
        @DisplayName("✅ 성공: 정상적인 요청 시 게시글이 생성되고 201 응답을 반환한다.")
        void createBoard_success() throws Exception {
            Map<String, String> request = Map.of(
                    "brdTitle", "테스트 제목",
                    "brdContent", "테스트 내용입니다."
            );

            mockMvc.perform(post("/api/v1/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.brdTitle").value("테스트 제목"))
                    .andExpect(jsonPath("$.data.memNickname").value(testMember.getNickname()));
        }

        @Test
        @WithMockUser(username = "testuser@example.com", roles = "USER")
        @DisplayName("❌ 실패(유효성): 제목이 비어있으면 400 Bad Request를 반환한다.")
        void createBoard_fail_blankTitle() throws Exception {
            Map<String, String> request = Map.of(
                    "brdTitle", "",
                    "brdContent", "내용은 있습니다."
            );
            mockMvc.perform(post("/api/v1/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ 실패(인증): 인증되지 않은 사용자는 401 Unauthorized를 반환한다.")
        void createBoard_fail_unauthorized() throws Exception {
            Map<String, String> request = Map.of(
                    "brdTitle", "인증되지 않은 제목",
                    "brdContent", "인증되지 않은 내용"
            );
            mockMvc.perform(post("/api/v1/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("게시글 조회 테스트")
    class GetBoard {

        @Test
        @DisplayName("✅ 성공: 게시글 ID로 상세 조회 시 200 응답과 함께 조회수가 1 증가한다.")
        void getBoardById_success() throws Exception {
            Board board = boardRepository.save(Board.builder()
                    .brdTitle("조회용 제목")
                    .brdContent("조회용 내용")
                    .member(testMember)
                    .build());
            int initialViews = board.getViews();

            mockMvc.perform(get("/api/v1/boards/" + board.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(board.getId()))
                    .andExpect(jsonPath("$.data.views").value(initialViews + 1));
        }

        @Test
        @DisplayName("❌ 실패(404): 존재하지 않는 게시글 ID로 조회 시 404 Not Found를 반환한다.")
        void getBoardById_fail_notFound() throws Exception {
            mockMvc.perform(get("/api/v1/boards/99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.message").value("해당 ID의 게시글을 찾을 수 없습니다: 99999"));
        }
    }

    @Nested
    @DisplayName("게시글 수정 테스트")
    class UpdateBoard {

        private Board testBoard;

        @BeforeEach
        void setUp() {
            testBoard = boardRepository.save(Board.builder()
                    .brdTitle("원본 제목")
                    .brdContent("원본 내용")
                    .member(testMember)
                    .build());
        }

        @Test
        @WithMockUser(username = "testuser@example.com", roles = "USER")
        @DisplayName("✅ 성공: 게시글 작성자가 수정 요청 시 200 응답과 함께 내용이 변경된다.")
        void updateBoard_success() throws Exception {
            Map<String, String> request = Map.of(
                    "brdTitle", "수정된 제목",
                    "brdContent", "수정된 내용입니다."
            );

            mockMvc.perform(put("/api/v1/boards/" + testBoard.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.brdTitle").value("수정된 제목"))
                    .andExpect(jsonPath("$.data.brdContent").value("수정된 내용입니다."));
        }

        @Test
        @WithMockUser(username = "otheruser@example.com", roles = "USER")
        @DisplayName("❌ 실패(권한): 작성자가 아닌 사용자가 수정 시도 시 403 Forbidden을 반환한다.")
        void updateBoard_fail_accessDenied() throws Exception {
            Map<String, String> request = Map.of(
                    "brdTitle", "수정 시도",
                    "brdContent", "권한 없음"
            );
            mockMvc.perform(put("/api/v1/boards/" + testBoard.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("게시글 삭제 테스트")
    class DeleteBoard {

        private Board testBoard;

        @BeforeEach
        void setUp() {
            testBoard = boardRepository.save(Board.builder()
                    .brdTitle("삭제될 제목")
                    .brdContent("삭제될 내용")
                    .member(testMember)
                    .build());
        }

        @Test
        @WithMockUser(username = "testuser@example.com", roles = "USER")
        @DisplayName("✅ 성공: 게시글 작성자가 삭제 요청 시 200 응답과 함께 게시글이 삭제된다.")
        void deleteBoard_success() throws Exception {
            mockMvc.perform(delete("/api/v1/boards/" + testBoard.getId())
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("게시글이 성공적으로 삭제되었습니다."));

            assertThat(boardRepository.findById(testBoard.getId())).isEmpty();
        }

        @Test
        @WithMockUser(username = "otheruser@example.com", roles = "USER")
        @DisplayName("❌ 실패(권한): 작성자가 아닌 사용자가 삭제 시도 시 403 Forbidden을 반환한다.")
        void deleteBoard_fail_accessDenied() throws Exception {
            mockMvc.perform(delete("/api/v1/boards/" + testBoard.getId())
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            assertThat(boardRepository.findById(testBoard.getId())).isPresent();
        }
    }
}