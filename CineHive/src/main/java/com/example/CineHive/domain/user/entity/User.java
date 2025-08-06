package com.example.CineHive.domain.user.entity;

import com.example.CineHive.domain.auth.entity.LoginHistory;
import com.example.CineHive.domain.auth.enums.ProviderType;
import com.example.CineHive.domain.auth.dto.RegisterRequest;
import com.example.CineHive.global.entity.BaseEntity;
import com.example.CineHive.domain.media.enums.Genre;
import com.example.CineHive.domain.user.enums.Gender;
import com.example.CineHive.domain.user.enums.UserRole;
import com.example.CineHive.domain.user.enums.UserType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 서비스의 사용자(회원)를 나타내는 핵심 엔티티입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?")
@Where(clause = "deleted_at IS NULL")
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

    @Column(name = "profile_image_url")
    private String profileImageUrl;

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

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Builder
    public User(String email, String password, String name, String nickname, String profileImageUrl, Gender gender, Set<Genre> genres, ProviderType provider, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl; // 빌더에 추가
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
                // profileImageUrl은 회원가입 시 기본값이 없거나, 별도로 설정할 수 있습니다.
                .gender(Gender.valueOf(dto.gender().toUpperCase()))
                .genres(dto.genres().stream()
                        .map(genreName -> Genre.valueOf(genreName.toUpperCase()))
                        .collect(Collectors.toSet()))
                .provider(ProviderType.LOCAL)
                .role(UserRole.ROLE_USER)
                .build();
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param newPassword 해시된 새로운 비밀번호
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    // --- 메서드 추가 ---
    public void changeProfileImage(String newProfileImageUrl) {
        this.profileImageUrl = newProfileImageUrl;
    }
    // --- ---

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

    /**
     * 회원 탈퇴 시 개인 식별 정보를 익명화합니다.
     */
    public void anonymize() {
        this.email = this.id + "@deleted.user";
        this.name = "탈퇴한 사용자";
        this.nickname = "탈퇴한 사용자";
        this.password = UUID.randomUUID().toString();
        this.profileImageUrl = null;
        this.genres.clear();
    }
}
