package com.example.CineHive.entity.oauth;

import com.example.CineHive.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kakao_users")
public class KakaoUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", unique = true)
    private String kakaoId; //카카오 고유 아이디


    @Column(length = 50)
    private String nickname;

    @Column(name = "mem_email", nullable = true)
    private String memEmail;

    @Column(name = "user_id")
    private Long userId; // users 테이블의 외래 키

    // User 엔티티와의 관계 설정
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "mem_id", insertable = false, updatable = false)
    private User user;

    @Column
    private String name;

    @ElementCollection
    @CollectionTable(name = "kakao_user_genres", joinColumns = @JoinColumn(name = "kakao_user_id"))
    @Column(name = "genre")
    private List<String> genres;

    public KakaoUser(String kakaoId, String nickname, String memEmail,String name, List<String> genres) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.memEmail = memEmail;
        this.name = name;
        this.genres = genres;
    }
}
