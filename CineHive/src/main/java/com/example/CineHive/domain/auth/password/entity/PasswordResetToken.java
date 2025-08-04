package com.example.CineHive.domain.auth.password.entity;

import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String selector;

    @Column(nullable = false)
    private String validatorHash;

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
