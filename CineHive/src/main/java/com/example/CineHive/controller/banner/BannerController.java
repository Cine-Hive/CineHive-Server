package com.example.CineHive.controller.banner;

import com.example.CineHive.dto.banner.BannerResponse;
import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.dto.response.ErrorResponse;
import com.example.CineHive.service.banner.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Banner Controller", description = "메인 화면 배너 등 사용자에게 노출되는 배너 조회 API")
@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "활성화된 배너 목록 조회",
            description = """
            사용자에게 보여줄 활성화 상태의 모든 배너를 순서대로 조회합니다.
            
            **참고: 배너의 추가, 수정, 활성화/비활성화는 관리자 API를 통해 이루어집니다.**
            - **관리자 API**: `POST, PUT, DELETE /api/v1/admin/banners`
            
            ### `imageUrl` 사용법
            응답에 포함된 `imageUrl`은 부분 경로이므로, 전체 이미지 URL을 만들려면 앞에 TMDB 이미지 서버 주소를 붙여야 합니다.
            클라이언트는 자신의 환경(웹/모바일)에 맞춰 최적의 이미지 크기를 선택하여 URL을 동적으로 생성해야 합니다.
            
            **공식: `[기본 URL]` + `[이미지 크기]` + `[파일 경로]`**
            
            - **기본 URL**: `https://image.tmdb.org/t/p/`
            - **이미지 크기 (권장)**:
                - **웹**: `w1280` 또는 `original`
                - **모바일**: `w780`
            - **파일 경로**: API 응답으로 받은 `imageUrl` 값 (예: `/jXJxMcVoEuXzym3pFgnq1thbdsE.jpg`)
            
            **최종 URL 예시 (웹):**
            `https://image.tmdb.org/t/p/w1280/jXJxMcVoEuXzym3pFgnq1thbdsE.jpg`
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getActiveBanners() {
        List<BannerResponse> banners = bannerService.findActiveBanners();
        return ResponseEntity.ok(ApiResponse.ok(banners));
    }
}