package com.example.CineHive.controller.board;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.CreateBoardRequest;
import com.example.CineHive.dto.board.GetListBoardDto;
import com.example.CineHive.dto.board.UpdateBoardRequest;
import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.service.board.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Board Controller", description = "게시글 CRUD 및 조회 API")
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<BoardDto>> createBoard(
            @Valid @RequestBody CreateBoardRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        BoardDto createdBoard = boardService.createBoard(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createdBoard));
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다. 조회수가 1 증가합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDto>> getBoardById(@PathVariable Long id) {
        BoardDto boardDto = boardService.getBoardById(id);
        return ResponseEntity.ok(ApiResponse.ok(boardDto));
    }

    @Operation(summary = "게시글 수정", description = "자신이 작성한 게시글의 제목과 내용을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardDto>> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBoardRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        BoardDto updatedBoard = boardService.updateBoard(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(updatedBoard));
    }

    @Operation(summary = "게시글 삭제", description = "자신이 작성한 게시글을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteBoard(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        boardService.deleteBoard(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "게시글이 성공적으로 삭제되었습니다.")));
    }

    @Operation(summary = "게시글 전체 목록 조회", description = "모든 게시글 목록을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetListBoardDto>>> getAllBoards() {
        List<GetListBoardDto> boards = boardService.getAllBoards();
        return ResponseEntity.ok(ApiResponse.ok(boards));
    }

    /*
    @Operation(summary = "게시글 검색", description = "키워드로 게시글을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BoardSearchDto>>> searchBoards(@RequestParam String keyword) {
        List<BoardSearchDto> results = boardService.searchBoards(keyword);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
    */
}