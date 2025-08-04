<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/auth/entity/PasswordResetToken.java
package com.example.CineHive.domain.auth.entity;

import com.example.CineHive.domain.user.entity.User;
=======
package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.user.User;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/auth/PasswordResetToken.java
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_prt_selector", columnList = "selector", unique = true)
})
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String selector;

    @Column(nullable = false)
    private String validatorHash;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    public PasswordResetToken(User user, String selector, String validatorHash, Instant expiryDate) {
        this.user = user;
        this.selector = selector;
        this.validatorHash = validatorHash;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    public void updateToken(String newSelector, String newValidatorHash, Instant newExpiryDate) {
        this.selector = newSelector;
        this.validatorHash = newValidatorHash;
        this.expiryDate = newExpiryDate;
    }
}