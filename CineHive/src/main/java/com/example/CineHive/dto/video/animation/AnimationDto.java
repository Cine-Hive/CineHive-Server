package com.example.CineHive.dto.video.animation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimationDto {
    private List<AnimationDTO> results;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnimationDTO {
        private long id;
        private String title;
        private String overview;
        private String posterPath;
        private String releaseDate;
        private List<Integer> genreIds;
        private double voteAverage;
        private double popularity;
    }
}
