package com.example.CineHive.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {
    private String memEmail;
    private String memPassword;

    // 기본 생성자
    public LoginDto() {}

    // 생성자
    public LoginDto(String memEmail, String memPassword) {
        this.memEmail = memEmail;
        this.memPassword = memPassword;
    }
}