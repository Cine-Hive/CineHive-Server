package com.example.CineHive.dto.tmdb;

import lombok.Data;

import java.util.List;

@Data
public class TmdbPagedResponse<T> {
    private int page;
    private List<T> results;
    private int total_pages;
    private int total_results;
}
