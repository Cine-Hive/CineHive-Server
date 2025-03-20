package com.example.CineHive.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDto {
    private Long id;
    private String memEmail;
    private LocalDateTime firstLoginDate;
    private LocalDateTime lastLoginDate;
    private String browser;
}

