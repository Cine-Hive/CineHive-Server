package com.example.CineHive.mapper;

import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;

import java.time.LocalDateTime;

public class UserMapper {
    public User toEntity(UserDto userDto) {
        User user = new User();
        user.setMemEmail(userDto.getMemEmail());
        user.setMemPw(userDto.getMemPassword());
        user.setMemName(userDto.getMemName());
        user.setMemSex(userDto.getMemSex());
        user.setMemNickname(userDto.getMemNickname());
        user.setMemRegisterDatetime(LocalDateTime.now());
        user.setGenres(userDto.getGenres());
        user.setMemType("일반");
        return user;
    }
}
