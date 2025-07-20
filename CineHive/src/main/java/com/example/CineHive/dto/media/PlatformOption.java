package com.example.CineHive.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "필터링 옵션으로 사용될 플랫폼 정보")
public record PlatformOption(
        @Schema(description = "플랫폼을 식별하는 값 (Enum의 name)", example = "NETFLIX")
        String value,
        @Schema(description = "UI에 표시될 플랫폼 이름", example = "Netflix")
        String label,
        @Schema(description = "플랫폼 로고 이미지 목록")
        List<LogoInfo> logos
) {}