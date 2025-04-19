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
@Schema(description = "출연진 정보")
public class CastDto {
    @Schema(description = "인물 ID", example = "819")
    private Long id;
    
    @Schema(description = "출연진 ID", example = "52fe4250c3a36847f8014a11")
    private Long castId;
    
    @Schema(description = "배우 이름", example = "브래드 피트")
    private String name;
    
    @Schema(description = "배역", example = "타일러 더든")
    private String character;
    
    @Schema(description = "프로필 이미지 경로", example = "/path/to/profile.jpg")
    private String profilePath;
    
    @Schema(description = "순서", example = "1")
    private Integer order;
} 