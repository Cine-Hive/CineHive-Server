package com.example.CineHive.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDto {
    private Long id;

    private Long memberId;
    private String memberNickname;

    private LocalDateTime firstLoginDate;
    private LocalDateTime lastLoginDate;
    private String browser;
}