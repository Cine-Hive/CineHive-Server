package com.example.CineHive.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String password;
    private String name;
    private String email;
    private String nickname;
    private String gender;
    private String type;
    private String registeredAt;
    private List<String> genres;
}