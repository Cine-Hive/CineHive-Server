package com.example.CineHive.entity.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // @CreatedDate 활성화를 위해 필요
@Table(name = "members") // DB 테이블 이름을 'members'로 명시적으로 지정
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 DB에 저장
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, length = 20)
    private String type; // "일반", "관리자" 등을 나타내는 필드

    @CreatedDate // 엔티티 생성 시각 자동 저장
    @Column(updatable = false)
    private LocalDateTime registeredAt;

    @ElementCollection(fetch = FetchType.LAZY) // 지연 로딩으로 성능 최적화
    @CollectionTable(name = "member_genres", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "genre")
    private Set<String> genres = new HashSet<>();

    @Builder
    public Member(String email, String password, String name, String nickname, Gender gender, Set<String> genres) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.gender = gender;
        this.type = "일반"; // 빌더에서 기본값 설정
        this.genres = (genres != null) ? genres : new HashSet<>();
    }

    //== 비즈니스 로직 (상태 변경 메서드) ==//

    /**
     * 비밀번호를 변경합니다.
     * @param newPassword 암호화된 새 비밀번호
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 닉네임을 변경합니다.
     * @param newNickname 새 닉네임
     */
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    /**
     * 선호 장르를 업데이트합니다.
     * @param newGenres 새로운 선호 장르 Set
     */
    public void updateGenres(Set<String> newGenres) {
        this.genres.clear();
        if (newGenres != null) {
            this.genres.addAll(newGenres);
        }
    }
}