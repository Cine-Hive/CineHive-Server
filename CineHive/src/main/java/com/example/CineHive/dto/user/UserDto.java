package com.example.CineHive.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private boolean exists; // 사용자 존재 여부
    private Long mem_id;
    private String memUserid;
    private String memPassword;
    private String memName;
    private String memEmail;
    private String memNickname;
    private String memSex;
    private String memType;
    private String memRegisterDatetime;
    private List<String> genres; // 추가된 장르 필드
    private String kakaoId;
    private String googleId;
    private String naverId;
}
