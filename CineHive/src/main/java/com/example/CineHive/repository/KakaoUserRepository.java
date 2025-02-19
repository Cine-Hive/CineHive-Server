package com.example.CineHive.repository;

import com.example.CineHive.entity.oauth.GoogleUser;
import com.example.CineHive.entity.oauth.KakaoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KakaoUserRepository extends JpaRepository<KakaoUser, Long> {

    Optional<KakaoUser> findByMemEmail(String memEmail);
}
