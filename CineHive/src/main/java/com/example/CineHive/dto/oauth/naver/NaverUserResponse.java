package com.example.CineHive.dto.oauth.naver;

/**
 * 네이버 사용자 정보 요청에 대한 전체 응답 DTO
 */
public record NaverUserResponse(
        String resultcode,
        String message,
        NaverUser response
) {}