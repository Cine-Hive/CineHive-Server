package com.example.CineHive.entity.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_history_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, updatable = false)
    private LocalDateTime firstLoginDate;

    @Column(nullable = false)
    private LocalDateTime lastLoginDate;

    @Column(length = 255)
    private String browser;

    public LoginHistory(Long id, Member member, LocalDateTime firstLoginDate, LocalDateTime lastLoginDate, String browser) {
        this.id = id;
        this.member = member;
        this.firstLoginDate = firstLoginDate;
        this.lastLoginDate = lastLoginDate;
        this.browser = browser;
    }

    /**
     * [추가된 부분]
     * 로그인 정보를 업데이트하는 비즈니스 메서드입니다.
     * 마지막 로그인 시간을 현재 시간으로, 브라우저 정보를 새로운 정보로 갱신합니다.
     * @param browser 새로운 브라우저 정보
     */
    public void updateLoginInfo(String browser) {
        this.lastLoginDate = LocalDateTime.now();
        this.browser = browser;
    }
}