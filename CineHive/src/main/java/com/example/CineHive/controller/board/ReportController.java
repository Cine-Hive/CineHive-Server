package com.example.CineHive.controller.board;

import com.example.CineHive.service.board.ReportService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report Controller", description = "게시글의 신고하기 기능을 제공하는 API")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "신고하기 등록", description = "특정 게시글에 대해 사용자가 신고하기를 등록")
    @PostMapping("/{boardId}")
    public ResponseEntity<String> reportBoard(
            @PathVariable Long boardId,
            @RequestBody String reason,
            HttpServletRequest request) {

        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String memEmail = jwtUtil.extractUsername(token);

            boolean isReported = reportService.reportBoard(memEmail, boardId, reason);

            return isReported ? ResponseEntity.ok("Reported")
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Reported");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }
}
