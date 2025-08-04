<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/user/entity/Gender.java
package com.example.CineHive.domain.user.entity;
=======
package com.example.CineHive.domain.user;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/user/Gender.java

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 성별을 정의하는 Enum 클래스입니다.
 */
@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE("남성"),
    FEMALE("여성"),
    OTHER("기타/비공개");

    private final String description;
}