<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/user/service/UserService.java
package com.example.CineHive.domain.user.service;
=======
package com.example.CineHive.domain.user;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/user/UserService.java

/**
 * 사용자 정보 조회 및 검증 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 * (인증 관련 책임은 AuthService로 이전되었습니다.)
 */
public interface UserService {

    /**
     * 이메일로 사용자를 조회합니다. (다른 서비스에서 내부적으로 사용)
     * @param email 조회할 이메일
     * @return 조회된 User 엔티티
     */
    User findUserByEmail(String email);
}