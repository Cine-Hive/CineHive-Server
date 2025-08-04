package com.example.CineHive.domain.account.service;

import com.example.CineHive.domain.account.dto.AccountInfoResponse;
import com.example.CineHive.domain.account.dto.UpdatePasswordRequest;
import com.example.CineHive.domain.account.dto.UpdatePreferencesRequest;
import com.example.CineHive.domain.account.dto.UpdateProfileRequest;

/**
 * 인증된 사용자의 계정 정보 관리를 위한 서비스 인터페이스입니다.
 */
public interface AccountService {

    /**
     * 현재 로그인된 사용자의 계정 정보를 조회합니다.
     * @param userEmail 사용자의 이메일 (Principal)
     * @return 계정 정보를 담은 응답 DTO
     */
    AccountInfoResponse getAccountInfo(String userEmail);

    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param userEmail 사용자의 이메일 (Principal)
     * @param request 비밀번호 변경 요청 정보
     */
    void changePassword(String userEmail, UpdatePasswordRequest request);

    /**
     * 사용자의 프로필 정보(닉네임 등)를 수정합니다.
     * @param userEmail 사용자의 이메일 (Principal)
     * @param request 프로필 수정 요청 정보
     * @return 수정된 계정 정보를 담은 응답 DTO
     */
    AccountInfoResponse updateProfile(String userEmail, UpdateProfileRequest request);

    /**
     * 사용자의 선호 설정(장르 등)을 수정합니다.
     * @param userEmail 사용자의 이메일 (Principal)
     * @param request 선호 설정 수정 요청 정보
     * @return 수정된 계정 정보를 담은 응답 DTO
     */
    AccountInfoResponse updatePreferences(String userEmail, UpdatePreferencesRequest request);

    /**
     * 사용자의 계정을 삭제(탈퇴)합니다.
     * @param userEmail 사용자의 이메일 (Principal)
     */
    void deleteAccount(String userEmail);
}
