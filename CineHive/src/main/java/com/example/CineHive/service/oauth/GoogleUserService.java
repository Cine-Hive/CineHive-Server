package com.example.CineHive.service.oauth;

import com.example.CineHive.dto.oauth.google.GoogleUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@Slf4j
public class GoogleUserService {


    @Getter
    @Value("${google.client.id}")
    private String clientId;

    @Getter
    @Value("${google.client.secret}")
    private String clientSecret;

    @Getter
    @Value("${google.redirect.uri}")
    private String redirectUri;


    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public GoogleUserService(UserRepository userRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    public GoogleUserInfo verifyIdTokenAndGetUserInfo(String idToken) throws IOException {
        String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        ResponseEntity<String> response;
        try {
            response = restTemplate.getForEntity(tokenInfoUrl, String.class);
        } catch (Exception e) {
            log.error("[Google ID Token] tokeninfo 호출 중 오류 발생", e);
            throw new IOException("Failed to call tokeninfo endpoint", e);
        }


        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonObject = new JSONObject(response.getBody());

            if (!clientId.equals(jsonObject.optString("aud"))) {
                log.error("[Google ID Token] 유효하지 않은 aud 값: {}", jsonObject.optString("aud"));
                throw new IOException("Invalid ID token: aud mismatch");
            }
            String issuer = jsonObject.optString("iss");
            if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
                log.error("[Google ID Token] 유효하지 않은 iss 값: {}", issuer);
                throw new IOException("Invalid ID token: iss mismatch");
            }
            long expirationTime = jsonObject.optLong("exp");
            if (Instant.now().getEpochSecond() >= expirationTime) {
                log.error("[Google ID Token] 토큰 만료됨: {}", Instant.ofEpochSecond(expirationTime));
                throw new IOException("ID token expired");
            }

            log.info("[Google ID Token] 토큰 검증 성공. 유저 정보: email={}, name={}",
                    jsonObject.optString("email"), jsonObject.optString("name"));

            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setMemNickname(jsonObject.optString("name", ""));
            userInfo.setMemEmail(jsonObject.optString("email", ""));

            return userInfo;
        } else {
            log.error("[Google ID Token] tokeninfo 호출 실패: 상태 코드 {}", response.getStatusCode());
            throw new IOException("Failed to verify ID token: " + response.getStatusCode());
        }
    }

    public String getAccessToken(String code) throws IOException {
        String url = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            return jsonObject.getString("access_token");
        } else {
            throw new IOException("Failed to get access token: " + response.getStatusCode());
        }
    }

    public GoogleUserInfo getUserInfo(String accessToken) throws IOException {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken;

        ResponseEntity<String> response = restTemplate.getForEntity(userInfoUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setMemNickname(jsonObject.getString("name"));
            userInfo.setMemEmail(jsonObject.getString("email"));
            return userInfo;
        } else {
            throw new IOException("Failed to get user info: " + response.getStatusCode());
        }
    }

    public void registerGoogleUser(UserDto userDto) {
        User newUser = new User();
        newUser.setMemEmail(userDto.getMemEmail());
        newUser.setMemPw("0");
        newUser.setMemNickname(userDto.getMemNickname());
        newUser.setMemName(userDto.getMemName());
        newUser.setMemSex(userDto.getMemSex());
        newUser.setMemRegisterDatetime(LocalDateTime.now());
        newUser.setMemType("구글");
        newUser.setGenres(userDto.getGenres());

        userRepository.save(newUser); // 사용자 정보 저장
    }
}