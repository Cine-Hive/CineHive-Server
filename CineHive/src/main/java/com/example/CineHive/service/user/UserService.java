package com.example.CineHive.service.user;

import com.example.CineHive.entity.user.User;

/**
 * 사용자 정보 조회 및 검증 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface UserService {

    /**
     * 이메일 사용 가능 여부를 확인합니다.
     * @param email 확인할 이메일
     * @return 사용 가능 시 true
     */
    boolean isEmailAvailable(String email);

    /**
     * 닉네임 사용 가능 여부를 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 사용 가능 시 true
     */
    boolean isNicknameAvailable(String nickname);

    /**
     * 이메일로 사용자를 조회합니다. (다른 서비스에서 내부적으로 사용)
     * @param email 조회할 이메일
     * @return 조회된 User 엔티티
     */
    User findUserByEmail(String email);
}