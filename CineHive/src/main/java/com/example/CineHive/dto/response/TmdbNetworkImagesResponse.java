package com.example.CineHive.dto.response;

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