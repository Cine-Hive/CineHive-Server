package com.example.CineHive.service.security;

import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Spring Security가 사용자 정보를 데이터베이스에서 조회할 때 사용하는 서비스입니다.
 * UserDetailsService 인터페이스를 구현합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 사용자 이름(여기서는 이메일)을 기반으로 사용자 정보를 로드합니다.
     * 이 메서드에서 반환된 UserDetails 객체를 기반으로 인증이 처리됩니다.
     *
     * @param username 사용자를 식별하는 이메일
     * @return 완전히 채워진 사용자 레코드 (null이 아님)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없거나 권한이 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByEmail(username)
                .map(this::createLoginMember)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    /**
     * Member 엔티티를 Spring Security가 사용하는 UserDetails 객체로 변환합니다.
     *
     * @param member 데이터베이스에서 조회한 Member 엔티티
     * @return UserDetails 객체
     */
    private UserDetails createLoginMember(Member member) {
        // Spring Security의 User 클래스를 사용합니다.
        // 생성자: new User(username, password, authorities)
        // 현재는 별도의 권한(Role)이 없으므로 빈 컬렉션을 전달합니다.
        return new User(
                member.getEmail(),
                member.getPassword(),
                Collections.emptyList() // TODO: 추후 권한(Role) 기능 추가 시 수정 필요
        );
    }
}
