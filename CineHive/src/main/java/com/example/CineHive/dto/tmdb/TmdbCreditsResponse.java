package com.example.CineHive.dto.tmdb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbCreditsResponse {
    private List<TmdbCastResponse> cast;
    private List<TmdbCrewResponse> crew;
}
