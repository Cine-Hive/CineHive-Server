package com.example.CineHive.dto.media;

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

