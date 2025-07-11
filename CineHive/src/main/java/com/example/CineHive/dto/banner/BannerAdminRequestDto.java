package com.example.CineHive.dto.banner;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class BannerAdminRequestDto {

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String title;

    private String subtitle;

    @NotBlank(message = "이미지 URL은 필수 입력 값입니다.")
    @URL(message = "올바른 URL 형식이어야 합니다.")
    private String imageUrl;

    @NotBlank(message = "링크 URL은 필수 입력 값입니다.")
    private String linkUrl;

    @Min(value = 1, message = "표시 순서는 1 이상이어야 합니다.")
    private int displayOrder;

    private boolean isActive;
}