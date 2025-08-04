<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/user/entity/UserType.java
package com.example.CineHive.domain.user.entity;
=======
package com.example.CineHive.domain.user;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/user/UserType.java

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 유형(분류)을 정의하는 Enum 클래스.
 * 권한(Role)과 별개로, 비즈니스 로직을 분기 처리하기 위해 사용됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserType {

    GENERAL("일반 회원"),
    CRITIC("평론가"),
    PAID("유료 회원");

    private final String description;
}