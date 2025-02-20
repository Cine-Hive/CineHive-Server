package com.example.CineHive.dto.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBoardDto {
    private String memEmail;
    private String brdTitle;
    private String brdContent;
}
