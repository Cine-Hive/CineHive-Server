package com.example.CineHive.dto.video.drama;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DramaDto {
    private List<DramaDTO> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DramaDTO{
        private long id;
        private String title;
        private String overview;
        private String posterPath;
        private String backDropPath;
        private String releaseDate;
        private List<Integer> genreIds;
        private double voteAverage;
        private double popularity;
    }

}
