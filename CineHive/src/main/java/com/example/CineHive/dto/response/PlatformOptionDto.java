package com.example.CineHive.dto.response;

import java.util.List;

/**
 * 플랫폼 필터 옵션을 위한 DTO.
 * 이제 단일 로고가 아닌, 사용 가능한 모든 로고 목록을 포함합니다.
 */
// 기존: public record PlatformOptionDto(String value, String label, String logoPath) {}
// 수정 후:
public record PlatformOptionDto(
        String value,
        String label,
        List<LogoDto> logos
) {}