package com.example.CineHive.repository.user;

import com.example.CineHive.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMemEmail(String memEmail);

    Optional<User> findByMemNickname(String memNickname);
}
