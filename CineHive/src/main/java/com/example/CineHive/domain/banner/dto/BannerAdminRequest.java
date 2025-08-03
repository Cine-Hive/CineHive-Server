package com.example.CineHive.domain.banner.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * 관리자가 배너를 생성하거나 수정할 때 사용하는 요청 DTO입니다.
 */
@Schema(description = "관리자용 배너 생성/수정 요청 DTO")
public record BannerAdminRequest(
        @Schema(description = "배너의 메인 제목", example = "CineHive 여름 특별 상영회")
        @NotBlank(message = "제목은 필수 입력 값입니다.")
        String title,

        @Schema(description = "배너의 부제목 (선택 사항)", example = "지금 바로 예매하세요!")
        String subtitle,

        @Schema(description = "배너에 표시될 이미지의 URL", example = "https://example.com/images/summer_banner.jpg")
        @NotBlank(message = "이미지 URL은 필수 입력 값입니다.")
        @URL(message = "올바른 URL 형식이어야 합니다.")
        String imageUrl,

        @Schema(description = "배너 클릭 시 이동할 링크 URL", example = "https://example.com/events/summer_special")
        @NotBlank(message = "링크 URL은 필수 입력 값입니다.")
        @URL(message = "올바른 URL 형식이어야 합니다.") // 링크 URL도 URL 형식이므로 추가
        String linkUrl,

        @Schema(description = "배너 표시 순서 (숫자가 작을수록 먼저 표시됨)", example = "1")
        @Min(value = 1, message = "표시 순서는 1 이상이어야 합니다.")
        int displayOrder,

        @Schema(description = "배너 활성화 여부 (true: 사용자에게 노출, false: 숨김)", example = "true")
        boolean isActive
) {}