package com.example.CineHive.domain.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoInfo {
    private String name;
    private String key;
    private String site;
    private String type;
}

