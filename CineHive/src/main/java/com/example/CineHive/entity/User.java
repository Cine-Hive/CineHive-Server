package com.example.CineHive.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mem_id;

    @Column(nullable = true)
    private String memUserid;

    @Column
    private String memPw;

    @Column(nullable = false, unique = true)
    private String memEmail;

    @Column
    private String memName;

    @Column(nullable = false)
    private String memNickname;

    @Column
    private String memSex;

    @Column
    private String memPhone;

    @Column(nullable = false)
    private LocalDateTime memRegisterDatetime;

    @Column(nullable = false)
    private String memType;

    @Column(name = "kakao_id", nullable = true)
    private String kakaoId; // 카카오 ID

    @Column(name = "naver_id", nullable = true)
    private String naverId; // 네이버 ID

    @Column(name = "google_id", nullable = true)
    private String googleId; // 카카오 ID

    @ElementCollection // 여러 개의 장르를 저장하기 위해 사용
    @CollectionTable(name = "user_genres", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "genre")
    private List<String> genres; // 사용자가 선택한 장르

    @PrePersist //필드를 자동으로 현재 시간으로 설정
    public void prePersist() {
        this.memRegisterDatetime = LocalDateTime.now();
    }


}