package com.example.CineHive.domain.media.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "미디어 이미지 정보")
@Builder
public record ImageInfo(
        @Schema(description = "이미지 파일 경로")
        String filePath,
        @Schema(description = "이미지 가로세로 비율")
        Double aspectRatio,
        @Schema(description = "이미지 높이")
        Integer height,
        @Schema(description = "이미지 너비")
        Integer width,
        @Schema(description = "이미지 평점")
        Double voteAverage,
        @Schema(description = "이미지 투표 수")
        Integer voteCount
) {}