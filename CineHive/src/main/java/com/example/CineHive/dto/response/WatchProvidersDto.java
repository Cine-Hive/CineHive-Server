package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WatchProvidersDto {
    private String link;
    private List<ProviderDto> streaming;  // flatrate
    private List<ProviderDto> rent;
    private List<ProviderDto> buy;
}