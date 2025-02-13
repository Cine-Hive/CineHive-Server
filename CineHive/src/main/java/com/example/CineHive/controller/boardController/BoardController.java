package com.example.CineHive.controller.boardController;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.service.UserService;
import com.example.CineHive.service.board.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}

