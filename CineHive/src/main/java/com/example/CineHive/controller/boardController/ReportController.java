package com.example.CineHive.controller.boardController;

import com.example.CineHive.service.board.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
public class ReportController {
    @Autowired
    private ReportService reportService;

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
