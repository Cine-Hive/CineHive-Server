package com.example.CineHive.controller.reply;

import com.example.CineHive.service.reply.ReplyJudgeService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/reply/judge")
@RequiredArgsConstructor
@Tag(name = "ReplyJudge Controller", description = "좋아요/싫어요 관련 기능을 제공하는 API")
public class ReplyJudgeController {

    @Autowired
    private ReplyJudgeService replyJudgeService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/like")
    @Operation(summary = "감상평에 좋아요 등록/취소", description = "reply_likes 테이블에 해당 감상평에 대한 좋아요 상태를 토글합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "'좋아요 완료' 또는 '좋아요 취소' 메시지 반환"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 유효하지 않음)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<String> toggleLike(
            @RequestParam Long movieId,
            @RequestParam Long replyId,
            HttpServletRequest request) {

        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            boolean isLiked = replyJudgeService.toggleLike(memEmail, movieId, replyId);
            return ResponseEntity.ok(isLiked ? "좋아요 완료" : "좋아요 취소");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @PostMapping("/dislike")
    @Operation(summary = "감상평에 싫어요 등록/취소", description = "reply_dislikes 테이블에 해당 감상평에 대한 싫어요 상태를 토글합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "'싫어요 완료' 또는 '싫어요 취소' 메시지 반환"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 유효하지 않음)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<String> toggleDislike(
            @RequestParam Long movieId,
            @RequestParam Long replyId,
            HttpServletRequest request) {

        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            boolean isDisliked = replyJudgeService.toggleDislike(memEmail, movieId, replyId);
            return ResponseEntity.ok(isDisliked ? "싫어요 완료" : "싫어요 취소");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @GetMapping("/count/like")
    @Operation(summary = "감상평의 좋아요 수 조회", description = "reply_likes 테이블에서 해당 감상평의 좋아요 수를 조회하여 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해당 감상평의 좋아요 수 (Long 타입) 반환"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<Long> getLikeCount(@RequestParam Long replyId) {
        long count = replyJudgeService.getLikeCount(replyId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/dislike")
    @Operation(summary = "감상평의 싫어요 수 조회", description = "reply_dislikes 테이블에서 해당 감상평의 싫어요 수를 조회하여 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해당 감상평의 싫어요 수 (Long 타입) 반환"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<Long> getDisLikeCount(@RequestParam Long replyId) {
        long count = replyJudgeService.getDisLikeCount(replyId);
        return ResponseEntity.ok(count);
    }
}
