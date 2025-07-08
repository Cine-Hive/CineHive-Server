package com.example.CineHive.controller.admin;

import com.example.CineHive.dto.admin.HomeChartSettingDto;
import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.dto.response.ErrorResponse;
import com.example.CineHive.service.admin.AdminSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Home Chart Controller", description = "관리자용 홈 화면 차트 설정 API")
@RestController
@RequestMapping("/api/v1/admin/home-charts")
@RequiredArgsConstructor
// TODO: Spring Security를 사용하여 이 컨트롤러에 대한 접근을 'ROLE_ADMIN'으로 제한해야 합니다.
// @PreAuthorize("hasRole('ADMIN')")
public class HomeChartAdminController {

    private final AdminSettingService adminSettingService;

    @Operation(summary = "홈 화면 차트 설정 조회", description = "현재 홈 화면에 표시되도록 설정된 차트 목록과 순서를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 역할 필요)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<HomeChartSettingDto>>> getHomeChartSettings() {
        List<HomeChartSettingDto> settings = adminSettingService.getHomeChartSettings().stream()
                .map(entity -> {
                    var dto = new HomeChartSettingDto();
                    dto.setChartType(entity.getChartType());
                    dto.setDisplayOrder(entity.getDisplayOrder());
                    return dto;
                }).toList();
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @Operation(summary = "선택 가능한 모든 차트 타입 조회", description = "관리자가 홈 화면 차트로 설정할 수 있는 모든 ChartType 목록을 제공합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ChartType>>> getAvailableChartTypes() {
        List<ChartType> chartTypes = adminSettingService.getAvailableChartTypes();
        return ResponseEntity.ok(ApiResponse.ok(chartTypes));
    }

    @Operation(
            summary = "홈 화면 차트 설정 업데이트",
            description = """
            홈 화면에 표시될 차트 목록 전체를 새로 업데이트합니다.
            **요청 본문에 포함된 목록으로 기존의 모든 설정이 대체(Overwrite)됩니다.**
            
            ### 요청 본문 (Request Body)
            `chartType`과 `displayOrder`를 포함하는 객체의 배열(List)을 전송해야 합니다.
            - `chartType`: 표시할 차트의 종류입니다. `/api/v1/admin/home-charts/available` 엔드포인트에서 제공하는 값을 사용합니다.
            - `displayOrder`: 차트의 표시 순서입니다. 낮은 숫자일수록 화면 상단에 먼저 표시됩니다.
            
            ### 주요 동작 방식
            - 이 API는 **전체 교체** 방식으로 동작합니다. 즉, 요청에 포함되지 않은 기존 차트 설정은 모두 삭제됩니다.
            - 성공적으로 처리되면 별도의 데이터 없이 성공 응답을 반환합니다.
            
            ⚠️ **주의사항**
            - **빈 배열 `[]`을 전송하면 홈 화면에 아무 차트도 표시되지 않으니 주의가 필요합니다.**
            - 이 API는 `ROLE_ADMIN` 권한이 있는 사용자만 호출할 수 있습니다.
            """
    )
    @RequestBody(
            description = "업데이트할 홈 화면 차트 설정 목록",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HomeChartSettingDto.class),
                    examples = @ExampleObject(
                            name = "차트 설정 예시",
                            value = """
                                    [
                                      {
                                        "chartType": "TRENDING_MOVIES_WEEK",
                                        "displayOrder": 1
                                      },
                                      {
                                        "chartType": "KOREAN_DRAMA_SERIES",
                                        "displayOrder": 2
                                      },
                                      {
                                        "chartType": "PIXAR_ANIMATION_COLLECTION",
                                        "displayOrder": 3
                                      }
                                    ]
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (요청 본문 형식 오류 또는 유효하지 않은 값)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateHomeChartSettings(@org.springframework.web.bind.annotation.RequestBody List<HomeChartSettingDto> settings) {
        adminSettingService.updateHomeChartSettings(settings);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}