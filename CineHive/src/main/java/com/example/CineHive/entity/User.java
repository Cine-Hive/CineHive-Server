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

    @Column
    private String memPw;

    @Column(nullable = false, unique = true)
    private String memEmail;

    @Column(nullable = true)
    private String memName;

    @Column(nullable = false)
    private String memNickname;

    @Column(nullable = true)
    private String memSex;

    @Column(nullable = false)
    private LocalDateTime memRegisterDatetime;

    @Column(nullable = false)
    private String memType;
    

    @ElementCollection // 여러 개의 장르를 저장하기 위해 사용
    @CollectionTable(name = "user_genres", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "genre")
    private List<String> genres; // 사용자가 선택한 장르


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoginHistory> loginHistories; // 로그인 히스토리 리스트

    @PrePersist //필드를 자동으로 현재 시간으로 설정
    public void prePersist() {
        this.memRegisterDatetime = LocalDateTime.now();
    }

}