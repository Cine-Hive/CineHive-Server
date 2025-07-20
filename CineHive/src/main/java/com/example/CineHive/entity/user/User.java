package com.example.CineHive.entity.user;

import com.example.CineHive.entity.BaseEntity; // 경로 수정
import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.entity.user.UserType; // 올바른 UserType import
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
// import org.hibernate.usertype.UserType; // 이 줄은 삭제

import java.util.HashSet;
import java.util.Set;

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
    private UserType type; // 이제 오류가 발생하지 않음

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
        this.type = UserType.GENERAL; // 이제 오류가 발생하지 않음
        this.genres = (genres != null) ? genres : new HashSet<>();
        this.provider = (provider != null) ? provider : ProviderType.LOCAL;
        this.role = (role != null) ? role : UserRole.ROLE_USER;
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
}