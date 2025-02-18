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
public class GoogleUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_id", unique = true)
    private String googleId; //구글 고유 아이디


    @Column(length = 50)
    private String nickname;

    @Column
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
    @CollectionTable(name = "google_user_genres", joinColumns = @JoinColumn(name = "google_user_id"))
    @Column(name = "genre")
    private List<String> genres;


    public GoogleUser(String googleId, String nickname, String name, String memEmail, List<String> genres) {
        this.googleId = googleId;
        this.nickname = nickname;
        this.name = name;
        this.memEmail = memEmail;
        this.genres = genres;
    }
}

