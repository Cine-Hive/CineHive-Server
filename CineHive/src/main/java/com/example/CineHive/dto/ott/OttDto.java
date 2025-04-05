package com.example.CineHive.dto.ott;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OttDto {
    private Long id;
    private String title;
    private String overview;
    private String posterPath;
    private Double popularity;
    private LocalDate releaseDate;
    private ProviderDto provider;
}
d