package com.example.CineHive.dto.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BoardSearchDto {
    private String brdTitle;
    private String brdContent;
    private String memNickname;
}
