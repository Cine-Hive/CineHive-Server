package com.example.CineHive.controller.admin;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.Report;
import com.example.CineHive.entity.post.ReportStatus;
import com.example.CineHive.entity.user.*;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.post.PostRepository;
import com.example.CineHive.repository.post.ReportRepository;
import com.example.CineHive.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminReportController의 API 엔드포인트에 대한 통합 테스트입니다.
 * 관리자(ADMIN) 권한에 따른 신고 조회 및 처리 기능의 동작을 검증합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AdminReportController 통합 테스트")
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    private Report pendingReport; // 테스트용 대기 상태 신고

    @BeforeEach
    void setUp() {
        reportRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User reporter = createMember("reporter@test.com", "신고자", UserRole.ROLE_USER);
        User reportedUser = createMember("reported@test.com", "피신고자", UserRole.ROLE_USER);
        createMember("admin@test.com", "관리자", UserRole.ROLE_ADMIN); // 관리자 계정 생성

        Post reportedPost = postRepository.save(Post.builder()
                .member(reportedUser)
                .brdTitle("신고 대상 게시글")
                .brdContent("문제 내용")
                .build());

        pendingReport = reportRepository.save(Report.builder()
                .reporter(reporter)
                .board(reportedPost)
                .reason("부적절한 내용")
                .build());
    }

    private User createMember(String email, String nickname, UserRole role) {
        return userRepository.save(User.builder()
                .email(email).password("password").name(nickname).nickname(nickname)
                .gender(Gender.MALE).provider(ProviderType.LOCAL).role(role)
                .build());
    }

    /**
     * 신고 처리(승인/거절) API에 대한 테스트
     */
    @Nested
    @DisplayName("신고 처리 (승인/거절) 테스트")
    class ProcessReport {

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("✅ 성공: 관리자가 신고를 '승인' 처리한다.")
        void acceptReport_byAdmin_success() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/accept", pendingReport.getId())
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("신고를 성공적으로 승인 처리했습니다."));

            Report processedReport = reportRepository.findById(pendingReport.getId()).orElseThrow();
            assertThat(processedReport.getStatus()).isEqualTo(ReportStatus.ACCEPTED);
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("✅ 성공: 관리자가 신고를 '기각' 처리한다.")
        void rejectReport_byAdmin_success() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/reject", pendingReport.getId())
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("신고를 성공적으로 기각 처리했습니다."));

            Report processedReport = reportRepository.findById(pendingReport.getId()).orElseThrow();
            assertThat(processedReport.getStatus()).isEqualTo(ReportStatus.REJECTED);
        }

        @Test
        @WithMockUser(username = "reporter@test.com", roles = "USER")
        @DisplayName("❌ 실패(권한): 일반 사용자가 신고 처리를 시도하면 403 Forbidden을 반환한다.")
        void processReport_byUser_fail() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/accept", pendingReport.getId())
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 실패(인증): 익명 사용자가 신고 처리를 시도하면 401 Unauthorized를 반환한다.")
        void processReport_byAnonymous_fail() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/accept", pendingReport.getId())
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("❌ 실패(409): 이미 처리된 신고를 다시 처리하려 하면 409 Conflict를 반환한다.")
        void processReport_alreadyProcessed_fail() throws Exception {
            // given: 신고를 미리 승인 상태로 변경
            pendingReport.accept();
            reportRepository.saveAndFlush(pendingReport);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/reject", pendingReport.getId())
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.message").value(ErrorCode.REPORT_ALREADY_PROCESSED.getMessage()));
        }
    }

    /**
     * 신고 내역 조회 API에 대한 테스트
     */
    @Nested
    @DisplayName("신고 내역 조회 테스트")
    class GetReports {

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("✅ 성공: 관리자가 모든 신고 내역을 성공적으로 조회한다.")
        void getReports_byAdmin_success() throws Exception {
            // given: PENDING 상태의 신고 하나 더 추가
            User reporter = userRepository.findByEmail("reporter@test.com").orElseThrow();
            User reportedUser = userRepository.findByEmail("reported@test.com").orElseThrow();
            Post anotherPost = postRepository.save(Post.builder().member(reportedUser).brdTitle("다른 게시글").brdContent("내용").build());
            reportRepository.save(Report.builder().reporter(reporter).board(anotherPost).reason("스팸").build());

            // when & then
            mockMvc.perform(get("/api/v1/admin/reports"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("✅ 성공: 관리자가 'PENDING' 상태의 신고 내역만 필터링하여 조회한다.")
        void getReports_filterByStatus_success() throws Exception {
            // given: 다른 상태의 신고 추가
            User reporter = userRepository.findByEmail("reporter@test.com").orElseThrow();
            User reportedUser = userRepository.findByEmail("reported@test.com").orElseThrow();
            Post anotherPost = postRepository.save(Post.builder().member(reportedUser).brdTitle("다른 게시글").brdContent("내용").build());
            Report acceptedReport = reportRepository.save(Report.builder().reporter(reporter).board(anotherPost).reason("스팸").build());
            acceptedReport.accept();
            reportRepository.save(acceptedReport);

            // when & then
            mockMvc.perform(get("/api/v1/admin/reports")
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        @Test
        @WithMockUser(username = "reporter@test.com", roles = "USER")
        @DisplayName("❌ 실패(권한): 일반 사용자가 신고 내역 조회를 시도하면 403 Forbidden을 반환한다.")
        void getReports_byUser_fail() throws Exception {
            mockMvc.perform(get("/api/v1/admin/reports"))
                    .andExpect(status().isForbidden());
        }
    }
}
