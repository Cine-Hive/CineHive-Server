package com.example.CineHive.dto.video.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpComingMovieDto {
    private Long id;
    private String posterPath;
}
