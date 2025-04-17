package com.example.CineHive.service.board;

import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Transactional
    public boolean reportBoard(String memEmail, Long boardId, String reason) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // 이미 신고했는지 확인
        Optional<Report> existingReport = reportRepository.findByUserAndBoard(user, board);
        if (existingReport.isPresent()) {
            throw new IllegalStateException("이미 신고한 게시글입니다.");
        }

        // 신고 등록
        Report report = new Report();
        report.setUser(user);
        report.setBoard(board);
        report.setReason(reason);
        reportRepository.save(report);

        // 신고 횟수 증가
        board.increaseReportCount();
        boardRepository.save(board);

        return true;
    }
}
