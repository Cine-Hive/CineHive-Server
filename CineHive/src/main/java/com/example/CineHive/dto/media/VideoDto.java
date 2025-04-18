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
@Schema(description = "비디오 정보")
public class VideoDto {
    @Schema(description = "비디오 ID", example = "5c9294240e0a267cd516835f")
    private String id;
    
    @Schema(description = "비디오 제목", example = "파이트 클럽 공식 예고편")
    private String name;
    
    @Schema(description = "비디오 키", example = "BdJKm16Co6M")
    private String key;
    
    @Schema(description = "비디오 사이트", example = "YouTube")
    private String site;
    
    @Schema(description = "비디오 유형", example = "Trailer")
    private String type;
} 