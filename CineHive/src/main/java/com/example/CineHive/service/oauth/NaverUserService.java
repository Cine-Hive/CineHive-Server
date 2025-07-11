
package com.example.CineHive.service.oauth;

import com.example.CineHive.dto.oauth.naver.NaverUserInfo;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import lombok.Getter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Scanner;


@Service
public class NaverUserService {

    @Getter
    @Value("${naver.client.id}")
    private String clientId;

    @Getter
    @Value("${naver.client.secret}")
    private String clientSecret;

    private final UserRepository userRepository;



    public NaverUserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public String getAccessToken(String code) throws IOException {
        String url = "https://nid.naver.com/oauth2.0/token?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=authorization_code" +
                "&code=" + code;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            return jsonObject.getString("access_token");
        } else {
            throw new IOException("Failed to get access token: " + responseCode);
        }
    }

    public NaverUserInfo getUserInfo(String accessToken) throws IOException {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        HttpURLConnection conn = (HttpURLConnection) new URL(userInfoUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONObject responseObject = jsonObject.getJSONObject("response");

            NaverUserInfo userInfo = new NaverUserInfo();
            userInfo.setMemEmail(responseObject.getString("email"));
            userInfo.setMemNickname(responseObject.getString("nickname"));

            return userInfo;
        } else {
            throw new IOException("Failed to get user info: " + responseCode);
        }
    }

    public void registerNaverUser(UserDto userDto) {
        User newUser = new User();
        newUser.setMemEmail(userDto.getMemEmail());
        newUser.setMemPw("0");
        newUser.setMemNickname(userDto.getMemNickname());
        newUser.setMemName(userDto.getMemName());
        newUser.setMemSex(userDto.getMemSex());
        newUser.setMemRegisterDatetime(LocalDateTime.now());
        newUser.setMemType("네이버");
        newUser.setGenres(userDto.getGenres());

        userRepository.save(newUser); // 사용자 정보 저장
    }
}