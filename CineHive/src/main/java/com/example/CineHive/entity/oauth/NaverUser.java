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
@Table(name = "naver_users")
public class NaverUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "naver_id", unique = true)
    private String naverId; //네이버 고유 아이디

    @Column(length = 50)
    private String nickname;

    @Column(name = "mem_userid", nullable = true)
    private String memUserId; //이메일

    @Column(name = "user_id")
    private Long userId;

    // User 엔티티와의 관계 설정
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "mem_id", insertable = false, updatable = false)
    private User user;

    @Column
    private String name;

    @ElementCollection
    @CollectionTable(name = "naver_user_genres", joinColumns = @JoinColumn(name = "naver_user_id"))
    @Column(name = "genre")
    private List<String> genres;

    public NaverUser(String naverId, String nickname, String memUserId, String name, List<String> genres) {
        this.naverId = naverId;
        this.nickname = nickname;
        this.memUserId = memUserId;
        this.name = name;
        this.genres = genres;
    }
}
