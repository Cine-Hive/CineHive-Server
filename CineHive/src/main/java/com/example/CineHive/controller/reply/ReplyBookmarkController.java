package com.example.CineHive.controller.reply;

import com.example.CineHive.dto.reply.ReplyDto;
import com.example.CineHive.service.reply.ReplyBookmarkService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply/bookmark")
@Tag(name = "ReplyBookMark Controller", description = "영화 즐겨찾기 관련 기능을 제공하는 API")
public class ReplyBookmarkController {

    @Autowired
    private ReplyBookmarkService replyBookmarkService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "즐겨찾기 추가/취소", description = "해당 영화에 대한 사용자의 즐겨찾기 상태를 토글합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "'즐겨찾기 완료' 또는 '즐겨찾기 취소' 메시지 반환"),
            @ApiResponse(responseCode = "401", description = "토큰이 없거나 유효하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    // 즐겨찾기 추가 요청
    @PostMapping("/toggle")
    public ResponseEntity<String> toggleBookmark(@RequestBody ReplyDto requestDto, HttpServletRequest request) {
        Long movieId = requestDto.getMovieId();

        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(401).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            boolean isBookmarked = replyBookmarkService.toggleBookmark(memEmail, movieId);
            return ResponseEntity.ok(isBookmarked ? "즐겨찾기 완료" : "즐겨찾기 취소");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }


    @Operation(summary = "해당 영화의 즐겨찾기 수 조회", description = "reply_bookmark 테이블에 해당 영화의 즐겨찾기 수를 movie_id로 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해당 영화의 즐겨찾기 수 (Long 타입) 반환"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/count")
    public ResponseEntity<Long> getBookmarkCount(@RequestParam Long movieId) {
        long count = replyBookmarkService.getBookmarkCount(movieId);
        return ResponseEntity.ok(count);
    }
}
