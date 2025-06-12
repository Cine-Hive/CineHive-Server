package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageDto {
    private String filePath;
    private Double aspectRatio;
    private Integer height;
    private Integer width;
    private Double voteAverage;
    private Integer voteCount;
}