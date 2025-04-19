package com.example.CineHive.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "장르 정보")
public class GenreDto {
    @Schema(description = "장르 ID", example = "18")
    private Integer id;
    
    @Schema(description = "장르명", example = "드라마")
    private String name;
} 