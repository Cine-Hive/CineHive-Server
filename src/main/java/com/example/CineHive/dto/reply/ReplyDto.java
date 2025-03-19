package com.example.CineHive.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReplyDto {
    private Long id;
    private String memEmail;
    private Long movieId;
    private String replyContent;
    private String memNickname;
    private LocalDateTime replyRegDate;
}
