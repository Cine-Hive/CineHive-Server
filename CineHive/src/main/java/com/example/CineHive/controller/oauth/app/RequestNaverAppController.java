package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.NaverUserInfo;
import com.example.CineHive.entity.oauth.NaverUser;
import com.example.CineHive.repository.NaverUserRepository;
import com.example.CineHive.service.oauth.NaverUserService;
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

@Tag(name = "Naver User App Controller", description = "네이버 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")

public class RequestNaverAppController {
    @Autowired
    private NaverUserService naverUserService;
    @Autowired
    private NaverUserRepository naverUserRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "네이버 앱 로그인", description = "앱에서 SDK를 실행 후 인증 및 로그인이 성공된 후, 앱에서 Access Token을 담아서 요청을 보내면 서버에서 json 데이터를 클라이언트에게 보내야 할 요청코드 ")
    @PostMapping("/naver/app-login")
    public ResponseEntity<?> naverAppLogin(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        try {
            String accessToken = requestBody.get("accessToken");
            NaverUserInfo userInfo = naverUserService.getUserInfo(accessToken);

            NaverUser naverUser = naverUserRepository.findByMemEmail(userInfo.getMemEmail()).orElse(null);
            boolean isNewUser = false;

            if (naverUser == null) {
                naverUser = naverUserService.registerNewNaverUser(userInfo);
                isNewUser = true;
            } else {
                log.info("기존 회원 로그인: {}", userInfo.getMemEmail());
            }

            // 세션에 사용자 정보 저장
            HttpSession session = request.getSession();
            session.setAttribute("user", userInfo);

            // JWT 토큰 생성
            String jwtToken = jwtUtil.generateToken(userInfo.getMemEmail());

            // 클라이언트에게 userInfo와 jwtToken 값을 반환
            Map<String, Object> response = new HashMap<>();
            response.put("userInfo", userInfo);
            response.put("jwtToken", jwtToken);

            if (isNewUser) {
                return ResponseEntity.status(201).body(response);
            } else {
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Naver login failed");
        }
    }
}
