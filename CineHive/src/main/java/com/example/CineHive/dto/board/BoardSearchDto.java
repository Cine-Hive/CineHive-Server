package com.example.CineHive.dto.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BoardSearchDto {
    private Long id;
    private LocalDateTime brgRedDate;
    private int likeCount;
    private int views;
    private String brdTitle;
    private String brdContent;
    private String memNickname;
}
