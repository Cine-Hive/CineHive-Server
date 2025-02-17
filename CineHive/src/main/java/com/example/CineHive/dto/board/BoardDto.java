package com.example.CineHive.dto.board;


import com.example.CineHive.entity.board.Board;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BoardDto {
    private Long id;
    private String brdTitle;
    private String brdContent;
    private String nickname;
    private String email;
    private LocalDateTime brgRedDate;
    private int bookmarkCount;
    private int likeCount;
    private int dislikeCount;
    private int reportCount;

    private int commentCount;
}
