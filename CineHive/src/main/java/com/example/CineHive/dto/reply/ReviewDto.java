package com.example.CineHive.dto.reply;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReviewDto {
    private Long id;
    private String reviewContent;
    private Long movieId;
    private String memNickname;
    private String memEmail;
}
