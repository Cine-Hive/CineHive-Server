package com.example.CineHive.controller.ott;

import com.example.CineHive.dto.ott.OttDto;
import com.example.CineHive.service.ott.OttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/ott")
@RequiredArgsConstructor
@Tag(name = "OTT Controller", description = "OTT 서비스별 영화 정보를 제공하는 API")
public class OttController {

    private final OttService ottService;

    @GetMapping("/{providerId}")
    @Operation(
            summary = "OTT 서비스별 영화 목록 조회",
            description = "제공된 OTT 서비스 ID에 해당하는 영화 목록을 조회하여 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTT 서비스에 해당하는 영화 목록 (List<OttDto>) 반환"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<List<OttDto>> getMoviesByProvider(@PathVariable int providerId) {
        List<OttDto> movies = ottService.getMoviesByProvider(providerId);
        return ResponseEntity.ok(movies);
    }
}
