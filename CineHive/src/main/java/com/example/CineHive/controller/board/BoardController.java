package com.example.CineHive.controller.board;

import com.example.CineHive.dto.board.*;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.service.board.BoardService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Board Controller", description = "게시글 CRUD, 검색, 전체 조회 기능을 제공하는 API")
@RestController
@RequestMapping("/boards")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "게시글 글 등록", description = "게시판 기능에서 글 등록")
    @PostMapping("/create")
    public ResponseEntity<Board> createBoard(@RequestBody CreateBoardDto createBoardDto, HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            createBoardDto.setMemEmail(email);
            Board createdBoard = boardService.createBoard(createBoardDto);
            return ResponseEntity.ok(createdBoard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "게시글 상세 페이지", description = "등록한 게시글에 대한 상세 페이지")
    @GetMapping("/detail/{id}")
    public ResponseEntity<BoardDto> getDetailBoard(@PathVariable Long id) {
        BoardDto boardDto = boardService.getBoardPostId(id);
        if (boardDto != null) {
            return ResponseEntity.ok(boardDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "게시글 글 수정", description = "사용자가 등록한 게시글에 대한 글을 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@PathVariable Long id,
                                             @RequestBody UpdateBoardRequest updatedBoard,
                                             HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            updatedBoard.setMemEmail(email);
            Board updatedBoardEntity = boardService.updateBoard(id, updatedBoard.getBrdTitle(),
                    updatedBoard.getBrdContent(), updatedBoard.getMemEmail());
            return ResponseEntity.ok(updatedBoardEntity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "게시글 글 삭제", description = "사용자가 등록한 게시글에 대한 삭제")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id, HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            boardService.deleteBoard(id, email);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @Operation(summary = "게시글 리스트 조회", description = "사용자들이 등록한 게시글들의 전체 목록을 조회")
    @GetMapping
    public ResponseEntity<List<GetListBoardDto>> getBoards() {
        List<GetListBoardDto> boards = boardService.getAllBoard();
        return new ResponseEntity<>(boards, HttpStatus.OK);
    }

    @Operation(summary = "게시글 검색", description = "제목, 내용 및 닉네임을 포함하여 등록한 게시글을 모두 검색")
    @GetMapping("/search")
    public ResponseEntity<List<BoardSearchDto>> searchBoards(@RequestParam String keyword) {
        List<BoardSearchDto> results = boardService.searchBoards(keyword);
        return ResponseEntity.ok(results);
    }
}
