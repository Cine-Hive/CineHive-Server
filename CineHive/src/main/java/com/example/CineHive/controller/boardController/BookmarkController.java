package com.example.CineHive.controller.boardController;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.service.board.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

}