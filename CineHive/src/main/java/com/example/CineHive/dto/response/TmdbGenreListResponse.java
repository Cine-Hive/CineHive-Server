package com.example.CineHive.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TmdbGenreListResponse {
    private List<TmdbGenreResponse> genres;
}