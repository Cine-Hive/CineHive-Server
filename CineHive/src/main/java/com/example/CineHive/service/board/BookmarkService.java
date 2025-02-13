package com.example.CineHive.service.board;

import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.repository.board.BookmarkRepository;
import com.example.CineHive.repository.UserRepository; // UserRepository 추가
import com.example.CineHive.repository.board.BoardRepository; // BoardRepository 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

}
