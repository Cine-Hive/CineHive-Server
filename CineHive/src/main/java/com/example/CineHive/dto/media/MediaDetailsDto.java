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
@Schema(description = "미디어 통합 상세 정보 응답 DTO")
public class MediaDetailsDto {
    @Schema(description = "미디어 기본 정보")
    private MediaItemDto media;
    
    @Schema(description = "출연진 정보 목록")
    private List<CastDto> credits;
    
    @Schema(description = "제작진 정보 목록")
    private List<CrewDto> crew;
    
    @Schema(description = "비디오 정보 목록")
    private List<VideoDto> videos;
    
    @Schema(description = "유사 미디어 목록")
    private List<MediaItemDto> similar;
} 