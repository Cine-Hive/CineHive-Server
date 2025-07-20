package com.example.CineHive.dto.media;

/**
 * 개별 로고 이미지 정보를 담는 DTO
 * @param filePath 로고 이미지의 경로
 * @param fileType 로고 파일의 확장자 (e.g., "svg", "png")
 */
public record LogoInfo(
        String filePath,
        String fileType
) {}