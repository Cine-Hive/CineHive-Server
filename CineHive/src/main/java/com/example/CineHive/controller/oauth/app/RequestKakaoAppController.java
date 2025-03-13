package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.KakaoUserInfo;
import com.example.CineHive.entity.oauth.KakaoUser;
import com.example.CineHive.repository.KakaoUserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
/*
앱에서 SDK를 실행해서 인증 및 로그인이 성공된 후, Access Token을 담아서 요청을 보내면 서버에서 json 데이터를 클라이언트에게 보냄
이때, 신규 회원이면 201로 반환하여 추가 기입 정보를 반환, 200이면 바로 로그인 (앱에서 201과 200으로 처리하면 될 거 같음)
 */
@Tag(name = "Kakao User App Controller", description = "카카오 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RequestKakaoAppController {

    @Autowired
    private KakaoUserService kakaoUserService;
    @Autowired
    private KakaoUserRepository kakaoUserRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "카카오 앱 로그인", description = "앱에서 SDK를 실행 후 인증 및 로그인이 성공된 후, 앱에서 Access Token을 담아서 요청을 보내면 서버에서 json 데이터를 클라이언트에게 보냄")
    @PostMapping("/kakao/app-login")
    public ResponseEntity<?> kakaoAppLogin(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        try {
            String accessToken = requestBody.get("accessToken");
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

            KakaoUser kakaoUser = kakaoUserRepository.findByMemEmail(userInfo.getMemEmail()).orElse(null);
            boolean isNewUser = false;

            if (kakaoUser == null) {
                log.info("신규 회원 등록 진행: {}", userInfo.getMemEmail());
                kakaoUser = kakaoUserService.registerNewKakaoUser(userInfo);
                isNewUser = true;
            } else {
                log.info("기존 회원 로그인: {}", userInfo.getMemEmail());
            }

            HttpSession session = request.getSession();
            session.setAttribute("user", userInfo);

            String jwtToken = jwtUtil.generateToken(userInfo.getMemEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("userInfo", userInfo);
            response.put("jwtToken", jwtToken);

            if (isNewUser) {
                return ResponseEntity.status(201).body(response);
            } else {
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Kakao login failed: ", e);
            return ResponseEntity.status(500).body("Kakao login failed");
        }
    }
}
