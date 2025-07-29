package com.example.CineHive.domain.user;

import com.example.CineHive.domain.auth.ProviderType;
import com.example.CineHive.global.common.BaseEntity;
import com.example.CineHive.domain.media.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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
     * 사용자의 비밀번호를 변경합니다.
     * @param newPassword 새 비밀번호 (암호화된 상태여야 함)
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 사용자의 닉네임을 변경합니다.
     * @param newNickname 새 닉네임
     */
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    /**
     * 사용자의 선호 장르를 업데이트합니다.
     * @param newGenres 새로운 선호 장르 Set
     */
    public void updateGenres(Set<Genre> newGenres) {
        this.genres.clear();
        if (newGenres != null) {
            this.genres.addAll(newGenres);
        }
    }
}