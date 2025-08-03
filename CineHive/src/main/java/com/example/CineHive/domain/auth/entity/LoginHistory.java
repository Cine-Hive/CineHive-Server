package com.example.CineHive.domain.auth.controller.entity;

import com.example.CineHive.domain.user.controller.User;
import com.example.CineHive.domain.common.controller.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자의 로그인 이력을 기록하는 엔티티입니다.
 * BaseEntity를 상속받아 생성 및 마지막 로그인 시간을 자동으로 관리합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "login_history")
public class LoginHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_history_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String browser;

    /**
     * LoginHistory 엔티티 생성을 위한 빌더입니다.
     * @param user 로그인한 사용자 엔티티
     * @param browser 로그인한 브라우저 정보
     */
    @Builder
    public LoginHistory(User user, String browser) {
        this.user = user;
        this.browser = browser;
    }

    /**
     * 로그인 정보를 업데이트합니다.
     * BaseEntity의 @LastModifiedDate가 마지막 로그인 시간 갱신을 자동으로 처리합니다.
     * @param browser 새로운 브라우저 정보
     */
    public void updateLoginInfo(String browser) {
        this.browser = browser;
    }
}