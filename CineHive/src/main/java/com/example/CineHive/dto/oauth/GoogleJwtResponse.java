package com.example.CineHive.dto.oauth;

import com.example.CineHive.dto.oauth.google.GoogleUserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleJwtResponse {

    private String jwtToken;
    private GoogleUserInfo userInfo;
}
