package com.example.CineHive.domain.auth.dto.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 네이버 사용자 정보 (response 객체 내부)
 */
public record NaverUser(
        String id,
        String nickname,
        String name,
        String email,
        String gender,
        String birthday,
        @JsonProperty("profile_image")
        String profileImage,
        String birthyear,
        String mobile
) {}