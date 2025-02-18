package com.example.CineHive.service.oauth;

import com.example.CineHive.dto.oauth.KakaoUserInfo;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.oauth.KakaoUser;
import com.example.CineHive.repository.KakaoUserRepository;
import com.example.CineHive.repository.UserRepository;
import lombok.Getter;
import org.json.JSONObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class KakaoUserService {

    @Getter
    @Value("${kakao.client.id}")
    private String clientId;

    @Getter
    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    @Getter
    @Value("${kakao.logout.redirect.uri}")
    private String logoutRedirectUri;

    @Autowired
    private KakaoUserRepository kakaouserRepository;

    @Autowired
    private UserRepository userRepository;

    private final OkHttpClient client = new OkHttpClient();

    public String getAccessToken(String code) throws IOException {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        FormBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", clientId)
                .add("redirect_uri", redirectUri)
                .add("code", code)
                .build();

        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                return jsonObject.getString("access_token");
            } else {
                throw new RuntimeException("Failed to get access token: " + response.message());
            }
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) throws IOException {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        Request request = new Request.Builder()
                .url(userInfoUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                System.out.println("Response Body: " + responseBody); // 응답 출력
                JSONObject jsonObject = new JSONObject(responseBody);
                KakaoUserInfo userInfo = new KakaoUserInfo();
                userInfo.setKakaoId(userInfo.getKakaoId());
                userInfo.setKakaoId(String.valueOf(jsonObject.getLong("id")));
                JSONObject properties = jsonObject.getJSONObject("properties");
                userInfo.setMemNickname(properties.getString("nickname"));

                // 이메일 필드가 있는지 확인
                if (jsonObject.has("kakao_account")) {
                    JSONObject kakaoAccount = jsonObject.getJSONObject("kakao_account");
                    if (kakaoAccount.has("email")) {
                        userInfo.setMemEmail(kakaoAccount.getString("email"));
                    } else {
                        userInfo.setMemEmail("이메일 미제공"); // 기본값 설정
                    }
                } else {
                    userInfo.setMemEmail("이메일 미제공");
                }
                return userInfo;
            } else {
                throw new RuntimeException("Failed to get user info: " + response.message());
            }
        }
    }


    public void registerUser(KakaoUserInfo userInfo) {
        KakaoUser socialUser = kakaouserRepository.findByKakaoId(userInfo.getKakaoId())
                .orElse(new KakaoUser(userInfo.getKakaoId(), userInfo.getMemNickname(), userInfo.getMemEmail(), null, null));
        kakaouserRepository.save(socialUser);
        }

    public KakaoUser registerNewKakaoUser(KakaoUserInfo userInfo) {
        // 먼저 User 엔티티에 사용자 정보 저장
        User user = new User();

        KakaoUser kakaoUser = new KakaoUser();
        kakaoUser.setKakaoId(userInfo.getKakaoId());
        kakaoUser.setNickname(userInfo.getMemNickname());
        kakaoUser.setMemEmail(userInfo.getMemEmail());
        kakaoUser.setName(userInfo.getMemName());
        kakaoUser.setGenres(userInfo.getGenres());
        kakaouserRepository.save(kakaoUser);

        return kakaoUser;
    }
}