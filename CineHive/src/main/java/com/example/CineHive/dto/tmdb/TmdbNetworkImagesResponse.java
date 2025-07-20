package com.example.CineHive.dto.tmdb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TmdbNetworkImagesResponse {
    private Long id;
    private List<TmdbLogoResponse> logos;
}