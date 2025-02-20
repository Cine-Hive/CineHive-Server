package com.example.CineHive.dto.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetListBoardDto {
    private Long id;
    private String brdTitle;
    private String brdContent;
    private String memNickname;
    private LocalDateTime brgRegDate; // 등록 날짜
    private int likeCount; // 좋아요 수
    private int views;

}
