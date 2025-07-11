package com.example.CineHive.service.account;

import com.example.CineHive.dto.account.AccountInfoResponseDto;
import java.util.List;

/**
 * 인증된 사용자의 계정 정보 관리를 위한 서비스 인터페이스.
 * 모든 메서드는 현재 로그인된 사용자를 기준으로 동작합니다.
 */
public interface AccountService {

    /**
     * 현재 로그인된 사용자의 계정 정보를 조회합니다.
     * @param email 사용자의 이메일 (Principal)
     * @return 계정 정보를 담은 DTO
     */
    AccountInfoResponseDto getAccountInfo(String email);

    /**
     * 사용자의 닉네임을 변경합니다.
     * @param email 사용자의 이메일 (Principal)
     * @param newNickname 변경할 새 닉네임
     */
    void changeNickname(String email, String newNickname);

    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param email 사용자의 이메일 (Principal)
     * @param oldPassword 기존 비밀번호
     * @param newPassword 변경할 새 비밀번호
     */
    void changePassword(String email, String oldPassword, String newPassword);

    /**
     * 사용자의 선호 장르 목록을 업데이트합니다.
     * @param email 사용자의 이메일 (Principal)
     * @param genres 새로운 선호 장르 목록
     */
    void updateGenres(String email, List<String> genres);

    /**
     * 사용자의 계정을 삭제(탈퇴)합니다.
     * 이 작업은 사용자와 관련된 모든 데이터를 영구적으로 삭제할 수 있습니다.
     * @param email 사용자의 이메일 (Principal)
     */
    void deleteAccount(String email);
}