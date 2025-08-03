package com.example.CineHive.domain.user.controller.entity;

import com.example.CineHive.domain.auth.controller.LoginHistory;
import com.example.CineHive.domain.auth.controller.ProviderType;
import com.example.CineHive.domain.auth.dto.RegisterRequest;
import com.example.CineHive.domain.common.controller.BaseEntity;
import com.example.CineHive.domain.media.controller.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 서비스의 사용자(회원)를 나타내는 핵심 엔티티입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_genres", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private Set<Genre> genres = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LoginHistory loginHistory;

    @Builder
    public User(String email, String password, String name, String nickname, Gender gender, Set<Genre> genres, ProviderType provider, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.gender = gender;
        this.type = UserType.GENERAL;
        this.genres = (genres != null) ? genres : new HashSet<>();
        this.provider = (provider != null) ? provider : ProviderType.LOCAL;
        this.role = (role != null) ? role : UserRole.ROLE_USER;
    }

    /**
     * RegisterRequest DTO로부터 User 엔티티를 생성하는 정적 팩토리 메서드입니다.
     * 비밀번호 암호화 및 기본값 설정을 포함합니다.
     *
     * @param dto             회원가입 요청 데이터
     * @param passwordEncoder 비밀번호 암호화기
     * @return 생성된 User 엔티티
     */
    public static User from(RegisterRequest dto, PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .name(dto.name())
                .nickname(dto.nickname())
                .gender(Gender.valueOf(dto.gender().toUpperCase()))
                .genres(dto.genres().stream()
                        .map(genreName -> Genre.valueOf(genreName.toUpperCase()))
                        .collect(Collectors.toSet()))
                .provider(ProviderType.LOCAL)
                .role(UserRole.ROLE_USER)
                .build();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updateGenres(Set<Genre> newGenres) {
        this.genres.clear();
        if (newGenres != null) {
            this.genres.addAll(newGenres);
        }
    }

    /**
     * 사용자의 로그인 기록을 업데이트합니다.
     * LoginHistory가 없으면 새로 생성하고, 있으면 기존 정보를 업데이트합니다.
     * @param browser 로그인한 브라우저 정보
     */
    public void updateLoginHistory(String browser) {
        if (this.loginHistory == null) {
            this.loginHistory = LoginHistory.builder()
                    .user(this)
                    .browser(browser)
                    .build();
        } else {
            this.loginHistory.updateLoginInfo(browser);
        }
    }
}
