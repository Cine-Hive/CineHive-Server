package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChartSummaryResponse {
    private List<ChartSection> sections;
}