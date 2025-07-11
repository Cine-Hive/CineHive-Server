package com.example.CineHive.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaChartDto {
    private Long mediaId;
    private String title;
    private String posterPath;
    private Double voteAverage;
    private boolean isAnimation;
    private int rank;
}
