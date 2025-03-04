package com.example.CineHive.controller.board;

import com.example.CineHive.service.board.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "신고하기 등록", description = "특정 게시글에 대해 사용자가 신고하기를 등록")
    @PostMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> reportBoard(
            @PathVariable Long boardId,
            @PathVariable String memEmail,
            @RequestParam String reason) {

        boolean isReported = reportService.reportBoard(memEmail, boardId, reason);
        return isReported ? ResponseEntity.ok("Reported")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already Reported");
    }
}
