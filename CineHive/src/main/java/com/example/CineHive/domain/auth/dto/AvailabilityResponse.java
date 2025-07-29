package com.example.CineHive.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용 가능 여부 확인 응답")
public record AvailabilityResponse(
        @Schema(description = "사용 가능 여부")
        boolean isAvailable
) {}