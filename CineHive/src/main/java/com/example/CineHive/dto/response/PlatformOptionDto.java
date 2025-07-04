package com.example.CineHive.dto.response;

/**
 * 플랫폼 필터 옵션을 위한 DTO.
 * value: API 호출 시 경로 변수로 사용될 값 (Enum의 이름)
 * label: UI에 사용자에게 보여줄 이름
 * logoPath: UI에 표시할 로고 이미지 경로
 */

public record PlatformOptionDto(
        String value,
        String label,
        String logoPath
) {}