package com.example.CineHive.controller.admin;

import com.example.CineHive.domain.banner.dto.BannerAdminRequest;
import com.example.CineHive.domain.banner.Banner;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.banner.BannerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminBannerController의 API 엔드포인트에 대한 통합 테스트입니다.
 * 관리자(ADMIN) 권한에 따른 배너 CRUD 기능의 동작을 검증합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AdminBannerController 통합 테스트")
class AdminBannerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BannerRepository bannerRepository;

    private Banner testBanner;

    @BeforeEach
    void setUp() {
        bannerRepository.deleteAll();
        testBanner = bannerRepository.save(Banner.builder()
                .title("기존 배너 제목")
                .subtitle("기존 부제")
                .imageUrl("https://example.com/old.jpg")
                .linkUrl("https://example.com/old-link")
                .displayOrder(1)
                .isActive(true)
                .build());
    }

    /**
     * 관리자(ROLE_ADMIN) 권한을 가진 사용자의 요청 시나리오를 테스트합니다.
     */
    @Nested
    @DisplayName("관리자 권한 (ROLE_ADMIN) 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    class AdminAccessTests {

        @Test
        @DisplayName("✅ 성공: 모든 배너 목록을 조회한다.")
        void getAllBanners_success() throws Exception {
            mockMvc.perform(get("/api/v1/admin/banners"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("✅ 성공: 새로운 배너를 생성한다.")
        void createBanner_success() throws Exception {
            // given
            BannerAdminRequest requestDto = new BannerAdminRequest("새 배너", "새 부제", "https://example.com/new.jpg", "https://example.com/new-link", 2, true);

            // when & then
            mockMvc.perform(post("/api/v1/admin/banners")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("새 배너"));

            assertThat(bannerRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("✅ 성공: 기존 배너를 수정한다.")
        void updateBanner_success() throws Exception {
            // given
            BannerAdminRequest requestDto = new BannerAdminRequest("수정된 배너", "수정된 부제", "https://example.com/updated.jpg", "https://example.com/updated-link", 1, false);

            // when & then
            mockMvc.perform(put("/api/v1/admin/banners/{bannerId}", testBanner.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 배너"))
                    .andExpect(jsonPath("$.data.active").value(false));

            Banner updatedBanner = bannerRepository.findById(testBanner.getId()).orElseThrow();
            assertThat(updatedBanner.getSubtitle()).isEqualTo("수정된 부제");
        }

        @Test
        @DisplayName("✅ 성공: 기존 배너를 삭제한다.")
        void deleteBanner_success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/v1/admin/banners/{bannerId}", testBanner.getId())
                            .with(csrf()))
                    .andExpect(status().isOk());

            assertThat(bannerRepository.findById(testBanner.getId())).isEmpty();
        }

        @Test
        @DisplayName("❌ 실패: 존재하지 않는 배너를 수정하려 하면 404 Not Found를 반환한다.")
        void updateBanner_fail_notFound() throws Exception {
            // given
            BannerAdminRequest requestDto = new BannerAdminRequest("수정", "수정", "https://example.com/u.jpg", "https://example.com/u", 1, true);
            long nonExistentId = 9999L;

            // when & then
            mockMvc.perform(put("/api/v1/admin/banners/{bannerId}", nonExistentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value(ErrorCode.BANNER_NOT_FOUND.getMessage()));
        }

        @ParameterizedTest(name = "실패(유효성): {0} 필드가 비어있을 때 400 Bad Request를 반환한다.")
        @CsvSource({
                "title, , 부제, https://a.com, https://b.com",
                "imageUrl, 제목, 부제, , https://b.com",
                "linkUrl, 제목, 부제, https://a.com, "
        })
        @DisplayName("❌ 실패(유효성): 필수 필드가 비어있을 때 400 Bad Request를 반환한다.")
        void createBanner_fail_withBlankField(String fieldName, String title, String subtitle, String imageUrl, String linkUrl) throws Exception {
            // given
            BannerAdminRequest requestDto = new BannerAdminRequest(title, subtitle, imageUrl, linkUrl, 1, true);

            // when & then
            mockMvc.perform(post("/api/v1/admin/banners")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 일반 사용자(ROLE_USER) 권한을 가진 사용자의 요청 시나리오를 테스트합니다.
     */
    @Nested
    @DisplayName("일반 사용자 권한 (ROLE_USER) 테스트")
    @WithMockUser(username = "user", roles = "USER")
    class UserAccessTests {

        @Test
        @DisplayName("❌ 실패(권한): 모든 배너 목록 조회 시 403 Forbidden을 반환한다.")
        void getAllBanners_fail_forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/admin/banners"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ 실패(권한): 배너 생성 시 403 Forbidden을 반환한다.")
        void createBanner_fail_forbidden() throws Exception {
            // given
            BannerAdminRequest requestDto = new BannerAdminRequest("해킹시도", "", "https://example.com/hack.jpg", "https://example.com/hack", 1, true);

            // when & then
            mockMvc.perform(post("/api/v1/admin/banners")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isForbidden());
        }
    }

    /**
     * 인증되지 않은 사용자의 요청 시나리오를 테스트합니다.
     */
    @Nested
    @DisplayName("인증되지 않은 사용자 테스트")
    @WithAnonymousUser
    class AnonymousAccessTests {

        @Test
        @DisplayName("❌ 실패(인증): 모든 배너 목록 조회 시 401 Unauthorized를 반환한다.")
        void getAllBanners_fail_unauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/admin/banners"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
