package com.example.CineHive.controller.admin;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.board.ReportStatus;
import com.example.CineHive.entity.member.*;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.ReportRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;

    private Member reporter; // 신고자
    private Member reportedUser; // 피신고자
    private Board reportedBoard; // 신고된 게시글
    private Report pendingReport; // 테스트용 대기 상태 신고

    @BeforeEach
    void setUp() {
        reportRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        reporter = createMember("reporter@test.com", "신고자", MemberRole.ROLE_USER);
        reportedUser = createMember("reported@test.com", "피신고자", MemberRole.ROLE_USER);
        createMember("admin@test.com", "관리자", MemberRole.ROLE_ADMIN); // 관리자 계정 생성

        reportedBoard = boardRepository.save(Board.builder()
                .member(reportedUser)
                .brdTitle("신고 대상 게시글")
                .brdContent("문제 내용")
                .build());

        pendingReport = reportRepository.save(Report.builder()
                .reporter(reporter)
                .board(reportedBoard)
                .reason("부적절한 내용")
                .build());
    }

    private Member createMember(String email, String nickname, MemberRole role) {
        return memberRepository.save(Member.builder()
                .email(email).password("password").name(nickname).nickname(nickname)
                .gender(Gender.MALE).provider(ProviderType.LOCAL).role(role)
                .build());
    }

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

            Report processedReport = reportRepository.findById(pendingReport.getId()).get();
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

            Report processedReport = reportRepository.findById(pendingReport.getId()).get();
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
        @DisplayName("❌ 실패(인증): 익명 사용자가 신고 처리를 시도하면 401 Unauthorized를 반환한다.")
        void processReport_byAnonymous_fail() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/accept", pendingReport.getId())
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("❌ 실패(400): 이미 처리된 신고를 다시 처리하려 하면 400 Bad Request를 반환한다.")
        void processReport_alreadyProcessed_fail() throws Exception {
            // given: 신고를 미리 승인 상태로 변경
            pendingReport.accept();
            reportRepository.saveAndFlush(pendingReport);

            // when & then
            mockMvc.perform(patch("/api/v1/admin/reports/{reportId}/reject", pendingReport.getId())
                            .with(csrf()))
                    .andExpect(status().isBadRequest()); // IllegalStateException은 400으로 처리
        }
    }

    @Nested
    @DisplayName("신고 내역 조회 테스트")
    class GetReports {

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("✅ 성공: 관리자가 모든 신고 내역을 성공적으로 조회한다.")
        void getReports_byAdmin_success() throws Exception {
            // PENDING 상태의 신고 하나 더 추가
            reportRepository.save(Report.builder().reporter(reporter).board(reportedBoard).reason("스팸").build());

            mockMvc.perform(get("/api/v1/admin/reports"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        @DisplayName("✅ 성공: 관리자가 'PENDING' 상태의 신고 내역만 필터링하여 조회한다.")
        void getReports_filterByStatus_success() throws Exception {
            // 다른 상태의 신고 추가
            Report acceptedReport = reportRepository.save(Report.builder().reporter(reporter).board(reportedBoard).reason("스팸").build());
            acceptedReport.accept();
            reportRepository.save(acceptedReport);

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