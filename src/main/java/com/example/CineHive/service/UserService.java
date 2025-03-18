package com.example.CineHive.service;

import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.mapper.UserMapper;
import com.example.CineHive.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.time.LocalDateTime;
import java.util.Optional;
@Service
public class UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean registerUser(UserDto userDto) {
        UserMapper userMapper = new UserMapper();
        User user = userMapper.toEntity(userDto);
        user.setMemPw(passwordEncoder.encode(userDto.getMemPassword())); // 비밀번호 암호화

        userRepository.save(user);
        return true;
    }


    public boolean loginUser(String memEmail, String memPassword) {
        // 사용자 ID로 사용자 조회
        Optional<User> existingUser = userRepository.findByMemEmail(memEmail);

        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        User user = existingUser.get();

        // 비밀번호 비교
        if (!passwordEncoder.matches(memPassword, user.getMemPw())) {
            throw new IllegalArgumentException("비밀번호가 맞지 않습니다.");
        }

        return true;
    }
    public boolean checkUserExists(String memEmail) {
        return userRepository.findByMemEmail(memEmail).isPresent();
    }
    public boolean checkUserExistsGoogle(String memEmail) {
        return userRepository.findByMemEmail(memEmail).isPresent();
    }

    public boolean checkUserExistsNaver(String memEmail) {
        return userRepository.findByMemEmail(memEmail).isPresent();
    }


    public User getUserInfo(String memEmail) {
        return userRepository.findByMemEmail(memEmail).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

}