package com.example.CineHive.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaSummaryDto {
    private Long id;
    private String title;
    private String posterPath;
    private Double voteAverage;
    private boolean isAnimation;
}
