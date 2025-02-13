package com.example.CineHive.dto.board;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
