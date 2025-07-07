package com.example.CineHive.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TmdbLogoResponse {
    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("file_type")
    private String fileType; // ".svg" 또는 ".png"
}