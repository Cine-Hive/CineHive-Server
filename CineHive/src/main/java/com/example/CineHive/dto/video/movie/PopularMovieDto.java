package com.example.CineHive.dto.video.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopularMovieDto {
    private Long id;
    private String posterPath;
    private String title;
}
