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
@Schema(description = "미디어 목록 응답 DTO")
public class MediaDto {
    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;

    @Schema(description = "미디어 아이템 목록", example = "[]")
    private List<MediaItemDto> results;
    
    @Schema(description = "전체 페이지 수", example = "100")
    private int totalPages;
    
    @Schema(description = "전체 결과 수", example = "2000")
    private int totalResults;
} 