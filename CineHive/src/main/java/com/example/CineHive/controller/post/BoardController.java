package com.example.CineHive.controller.post;

import com.example.CineHive.dto.post.PostDetailResponse;
import com.example.CineHive.dto.post.PostSortType;
import com.example.CineHive.dto.post.CreatePostRequest;
import com.example.CineHive.dto.post.PostSummaryResponse;
import com.example.CineHive.dto.post.UpdatePostRequest;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.tmdb.PagedResponse;
import com.example.CineHive.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Board Controller", description = "게시글 CRUD 및 조회 API")
@Validated
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final PostService postService;

    @Operation(summary = "게시글 생성",
            description = "새로운 게시글을 등록합니다. 제목과 내용은 필수이며, 인증된 사용자의 정보가 작성자로 자동 등록됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "게시글 생성 성공", content = @Content(schema = @Schema(implementation = PostDetailResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패 (제목 또는 내용 누락)", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createBoard(
            @Valid @RequestBody CreatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse createdBoard = postService.createBoard(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createdBoard));
    }

    @Operation(summary = "게시글 상세 조회",
            description = "특정 ID를 가진 게시글의 상세 정보를 조회합니다. 이 API 호출 시 해당 게시글의 조회수가 1 증가합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공", content = @Content(schema = @Schema(implementation = PostDetailResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getBoardById(@PathVariable Long id) {
        PostDetailResponse postDetailResponse = postService.getBoardById(id);
        return ResponseEntity.ok(ApiResponse.ok(postDetailResponse));
    }

    @Operation(summary = "게시글 수정",
            description = "자신이 작성한 게시글의 제목과 내용을 수정합니다. 타인의 게시글은 수정할 수 없습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 수정 성공", content = @Content(schema = @Schema(implementation = PostDetailResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한이 없는 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailResponse updatedBoard = postService.updateBoard(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(updatedBoard));
    }

    @Operation(summary = "게시글 삭제",
            description = "자신이 작성한 게시글을 삭제합니다. 타인의 게시글은 삭제할 수 없습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한이 없는 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteBoard(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        postService.deleteBoard(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "게시글이 성공적으로 삭제되었습니다.")));
    }

    @Operation(summary = "게시글 목록 페이징 조회",
            description = "게시글 목록을 페이징하여 조회합니다. 쿼리 파라미터를 통해 페이지 번호, 페이지 크기, 정렬 기준을 지정할 수 있습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공", content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostSummaryResponse>>> getBoards(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "페이지당 게시글 수", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "정렬 기준", schema = @Schema(implementation = PostSortType.class), example = "LATEST")
            @RequestParam(defaultValue = "LATEST") PostSortType sort) {
        PagedResponse<PostSummaryResponse> pagedResponse = postService.getBoards(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(pagedResponse));
    }
}