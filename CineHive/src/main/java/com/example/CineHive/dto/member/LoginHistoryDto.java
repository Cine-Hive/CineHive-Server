package com.example.CineHive.dto.member;

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

    // [수정] 회원의 이메일 대신, 회원의 고유 ID를 참조하도록 변경
    private Long memberId;
    private String memberNickname;

    private LocalDateTime firstLoginDate;
    private LocalDateTime lastLoginDate;
    private String browser;
}