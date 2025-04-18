package com.example.CineHive.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "미디어 출연/제작진 정보")
public class MediaCreditsDto {
    @Schema(description = "미디어 ID", example = "550")
    private Long id;
    
    @Schema(description = "출연진 목록")
    private List<CastDto> cast;
    
    @Schema(description = "제작진 목록")
    private List<CrewDto> crew;
} 