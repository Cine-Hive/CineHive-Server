package com.example.CineHive.service.security;

import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Spring Security가 사용자 정보를 데이터베이스에서 조회할 때 사용하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 이메일을 기반으로 사용자 정보를 로드합니다.
     * 이 메서드에서 반환된 UserDetails 객체를 기반으로 인증 및 인가가 처리됩니다.
     *
     * @param username 사용자를 식별하는 이메일
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    /**
     * User 엔티티를 Spring Security가 사용하는 UserDetails 객체로 변환합니다.
     *
     * @param user 데이터베이스에서 조회한 User 엔티티
     * @return UserDetails 객체
     */
    private UserDetails createUserDetails(User user) {
        // 사용자의 Role을 GrantedAuthority로 변환합니다.
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities // 사용자의 권한(Role) 목록 전달
        );
    }
}