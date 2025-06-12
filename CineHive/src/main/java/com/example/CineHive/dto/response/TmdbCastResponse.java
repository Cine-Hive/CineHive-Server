package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbCastResponse {
    private Long id;
    private String name;
    private String profile_path;
    private String character;
    private Integer order;
}
