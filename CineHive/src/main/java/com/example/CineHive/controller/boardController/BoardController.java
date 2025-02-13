package com.example.CineHive.controller.boardController;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.service.board.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class BoardController {
    @Autowired
    private BoardService boardService;

    @PostMapping("/boards/create")
    public ResponseEntity<Board> createBoard(@RequestBody BoardDto boardDto){

        Board createdBoard = boardService.createBoard(boardDto);
        return ResponseEntity.ok(createdBoard);
    }

    @GetMapping("/boards/detail/{id}")
    public ResponseEntity<BoardDto> getDetailBoard(@PathVariable Long id){
        BoardDto boardDto = boardService.getBoardPostId(id);
        if(boardDto != null){
            return ResponseEntity.ok(boardDto);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/boards/{id}")
    public ResponseEntity<Board> updateBoard(@PathVariable Long id, @RequestBody Board updatedBoard){
        Board updateBoard = boardService.updateBoard(id, updatedBoard.getBrdTitle(), updatedBoard.getBrdContent());
        return ResponseEntity.ok(updateBoard);
    }

    @DeleteMapping("/boards/delete/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id){
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/boards")
    public ResponseEntity<List<BoardDto>> getBoards() {
        List<BoardDto> boards = boardService.getAllBoard();
        return new ResponseEntity<>(boards, HttpStatus.OK);
    }
}

