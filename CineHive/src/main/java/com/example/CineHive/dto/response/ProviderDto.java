package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProviderDto {
    private Long providerId;
    private String providerName;
    private String logoPath;
    private Integer displayPriority;
}