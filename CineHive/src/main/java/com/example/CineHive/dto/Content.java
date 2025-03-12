package com.example.CineHive.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private Long id;
    private String name;
    private String overview;
    private String posterPath;
    private String genre;
}
