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
public class TmdbKeywordsResponse {
    private List<TmdbKeywordResponse> keywords; // 영화용
    private List<TmdbKeywordResponse> results;  // TV 시리즈용
}