<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/user/service/UserServiceImpl.java
package com.example.CineHive.domain.user.service;

import com.example.CineHive.domain.user.repository.UserRepository;
=======
package com.example.CineHive.domain.user;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/user/UserServiceImpl.java

import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}