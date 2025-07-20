package com.example.CineHive.dto.tmdb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbVideoResponse {
    private String id;
    private String name;
    private String key;
    private String site;
    private Integer size;
    private String type;
    private Boolean official;
    private String published_at;
}