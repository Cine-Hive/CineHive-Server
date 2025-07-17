package com.example.CineHive.controller.banner;

import com.example.CineHive.dto.banner.BannerResponseDto;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
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

/**
 * BannerController의 API 엔드포인트에 대한 단위 테스트입니다.
 * BannerService를 Mocking하여 컨트롤러 계층의 로직만 고립시켜 검증합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("BannerController 테스트")
class BannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BannerService bannerService;

    /**
     * Given: BannerService가 활성화된 배너 목록을 반환하도록 설정
     * When: GET /api/v1/banners API를 호출하면
     * Then: HTTP 200 OK 상태 코드와 함께 배너 목록이 정상적으로 반환된다.
     */
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

    /**
     * Given: BannerService가 빈 배너 목록을 반환하도록 설정
     * When: GET /api/v1/banners API를 호출하면
     * Then: HTTP 200 OK 상태 코드와 함께 빈 배열이 데이터로 반환된다.
     */
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

    /**
     * Given: BannerService가 예외를 발생시키도록 설정
     * When: GET /api/v1/banners API를 호출하면
     * Then: GlobalExceptionHandler에 의해 HTTP 500 Internal Server Error 상태 코드와 표준 에러 응답이 반환된다.
     */
    @Test
    @DisplayName("❌ 실패: 서비스 계층에서 예외가 발생할 경우, 500 Internal Server Error를 반환한다.")
    void getActiveBanners_fail_whenServiceThrowsException() throws Exception {
        // given
        // 서비스가 BusinessException을 던지는 상황을 시뮬레이션
        given(bannerService.findActiveBanners()).willThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

        // when & then
        mockMvc.perform(get("/api/v1/banners"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
