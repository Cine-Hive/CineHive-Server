package com.example.CineHive.controller.admin;

import com.example.CineHive.dto.admin.HomeChartSettingRequest;
import com.example.CineHive.dto.admin.HomeChartSettingResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.ErrorResponse;
import com.example.CineHive.service.admin.AdminHomeChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Home Chart Controller", description = "관리자용 홈 화면 차트 설정 API")
@RestController
@RequestMapping("/api/v1/admin/home-charts")
@RequiredArgsConstructor
public class AdminHomeChartController {

    private final AdminHomeChartService adminHomeChartService;

    @Operation(summary = "홈 화면 차트 설정 조회",
            description = "현재 홈 화면에 표시되도록 설정된 차트 목록과 순서를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<HomeChartSettingResponse>>> getHomeChartSettings() {
        List<HomeChartSettingResponse> settings = adminHomeChartService.getHomeChartSettings();
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @Operation(summary = "선택 가능한 모든 차트 타입 조회",
            description = "관리자가 홈 화면 차트로 설정할 수 있는 모든 `ChartType` 목록을 제공합니다.")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ChartType>>> getAvailableChartTypes() {
        List<ChartType> chartTypes = adminHomeChartService.getAvailableChartTypes();
        return ResponseEntity.ok(ApiResponse.ok(chartTypes));
    }

    @Operation(summary = "홈 화면 차트 설정 업데이트",
            description = "홈 화면에 표시될 차트 목록 전체를 새로 업데이트합니다. 요청 본문에 포함된 목록으로 기존의 모든 설정이 대체(Overwrite)됩니다.")
    @RequestBody(
            description = "업데이트할 홈 화면 차트 설정 목록",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = HomeChartSettingRequest.class),
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
                                      }
                                    ]
                                    """
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (요청 본문 형식 오류 또는 유효성 검사 실패)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<ApiResponse<MessageResponse>> updateHomeChartSettings(
            @Valid @org.springframework.web.bind.annotation.RequestBody List<HomeChartSettingRequest> settings) {
        adminHomeChartService.updateHomeChartSettings(settings);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("홈 화면 차트 설정이 성공적으로 업데이트되었습니다.")));
    }
}