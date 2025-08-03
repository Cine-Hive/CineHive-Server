package com.example.CineHive.domain.account;

import com.example.CineHive.domain.user.dto.AccountInfoResponse;
import com.example.CineHive.domain.user.dto.UpdateGenresRequest;
import com.example.CineHive.domain.user.dto.UpdateNicknameRequest;
import com.example.CineHive.domain.user.dto.UpdatePasswordRequest;

/**
 * 인증된 사용자의 계정 정보 관리를 위한 서비스 인터페이스입니다.
 */
public interface AccountService {

    /**
     * 현재 로그인된 사용자의 계정 정보를 조회합니다.
     * @param email 사용자의 이메일 (Principal)
     * @return 계정 정보를 담은 응답
     */
    AccountInfoResponse getAccountInfo(String email);

    /**
     * 사용자의 닉네임을 변경합니다.
     * @param email 사용자의 이메일 (Principal)
     * @param request 닉네임 변경 요청 정보
     */
    void changeNickname(String email, UpdateNicknameRequest request);

    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param email 사용자의 이메일 (Principal)
     * @param request 비밀번호 변경 요청 정보
     */
    void changePassword(String email, UpdatePasswordRequest request);

    /**
     * 사용자의 선호 장르 목록을 업데이트합니다.
     * @param email 사용자의 이메일 (Principal)
     * @param request 선호 장르 변경 요청 정보
     */
    void updateGenres(String email, UpdateGenresRequest request);

    /**
     * 사용자의 계정을 삭제(탈퇴)합니다.
     * @param email 사용자의 이메일 (Principal)
     */
    void deleteAccount(String email);
}