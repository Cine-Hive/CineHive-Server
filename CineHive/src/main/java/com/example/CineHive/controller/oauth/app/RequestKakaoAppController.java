package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.KakaoUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.service.UserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Tag(name = "Kakao User App Controller", description = "카카오 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RequestKakaoAppController {

    @Autowired
    private KakaoUserService kakaoUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/kakao/login")
    public ResponseEntity<?> loginWithKakao(@RequestBody String accessToken) {
        try {
            // 엑세스 토큰으로 사용자 정보 가져오기
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

            // DB에서 사용자 검색
            Optional<User> user = userRepository.findByMemEmail(userInfo.getMemEmail());

            if (user != null) {
                // 기존 사용자: JWT 토큰 생성 및 반환
                String jwtToken = jwtUtil.generateToken(userInfo.getMemEmail());
                return ResponseEntity.ok(Map.of("token", jwtToken, "user", userInfo));
            } else {
                // 신규 사용자: 사용자 정보를 세션에 저장
                return ResponseEntity.status(201).body(userInfo); // 추가 정보 입력을 위한 응답
            }
        } catch (Exception e) {
            log.error("Error during Kakao login", e);
            return ResponseEntity.status(500).body("로그인 처리 중 오류 발생");
        }
    }

    @PostMapping("/kakao/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        // 사용자 정보를 DB에 저장하는 로직
        userService.registerUser(userDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
