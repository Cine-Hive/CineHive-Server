package com.example.CineHive.controller.banner;

import com.example.CineHive.dto.banner.BannerResponseDto;
import com.example.CineHive.service.banner.BannerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("BannerController 테스트")
class BannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BannerService bannerService;

    @Test
    @DisplayName("✅ 성공: 활성화된 배너가 있을 경우, 200 OK와 함께 배너 목록을 반환한다.")
    void getActiveBanners_success_withContent() throws Exception {
        // given
        BannerResponseDto banner1 = new BannerResponseDto(1L, "배너 제목 1", "부제 1", "/img1.jpg", "/link1");
        BannerResponseDto banner2 = new BannerResponseDto(2L, "배너 제목 2", "부제 2", "/img2.jpg", "/link2");
        List<BannerResponseDto> dummyBanners = List.of(banner1, banner2);

        given(bannerService.findActiveBanners()).willReturn(dummyBanners);

        // when & then
        mockMvc.perform(get("/api/v1/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("배너 제목 1"))
                .andExpect(jsonPath("$.data[1].linkUrl").value("/link2"));
    }

    @Test
    @DisplayName("✅ 성공: 활성화된 배너가 없을 경우, 200 OK와 함께 빈 목록을 반환한다.")
    void getActiveBanners_success_withEmptyList() throws Exception {
        // given
        given(bannerService.findActiveBanners()).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @DisplayName("❌ 실패: 서비스 계층에서 예외가 발생할 경우, 500 Internal Server Error를 반환한다.")
    void getActiveBanners_fail_whenServiceThrowsException() throws Exception {
        // given
        given(bannerService.findActiveBanners()).willThrow(new RuntimeException("DB 연결 오류"));

        // when & then
        mockMvc.perform(get("/api/v1/banners"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("서버 내부에서 예상치 못한 오류가 발생했습니다. 관리자에게 문의하세요."));
    }
}