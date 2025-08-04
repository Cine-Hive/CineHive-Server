<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/auth/entity/PasswordHistory.java
package com.example.CineHive.domain.auth.entity;

import com.example.CineHive.domain.user.entity.User;
=======
package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.user.User;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/auth/PasswordHistory.java
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "password_history", indexes = {
        @Index(name = "idx_password_history_user_id", columnList = "user_id")
})
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public PasswordHistory(User user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
    }
}