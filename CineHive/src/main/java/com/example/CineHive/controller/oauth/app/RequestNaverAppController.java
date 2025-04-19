package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.NaverUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.oauth.NaverUserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@Tag(name = "Naver User App Controller", description = "네이버 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")

public class RequestNaverAppController {
    @Autowired
    private NaverUserService naverUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "네이버 앱 로그인", description = "앱에서 SDK를 실행 후 인증 및 로그인이 성공된 후, 앱에서 Access Token을 담아서 요청을 보내면 서버에서 json 데이터를 클라이언트에게 보내야 할 요청코드 ")
    @PostMapping("/naver/app-login")
    public ResponseEntity<?> naverAppLogin(@RequestBody String accessToken) {
        try {
            NaverUserInfo userInfo = naverUserService.getUserInfo(accessToken);

            Optional<User> user = userRepository.findByMemEmail(userInfo.getMemEmail());

            if (user != null) {

                String jwtToken = jwtUtil.generateToken(userInfo.getMemEmail());
                return ResponseEntity.ok(Map.of("token", jwtToken, "user", userInfo));
            } else {

                return ResponseEntity.status(201).body(userInfo);
            }
        } catch (Exception e) {
            log.error("Error during Kakao login", e);
            return ResponseEntity.status(500).body("로그인 처리 중 오류 발생");
        }
    }

    @Operation(summary = "네이버 사용자 등록", description = "네이버 사용자 정보를 입력받아 회원가입을 진행, 중복 검사 통과 후 naver_user 테이블에 저장")
    @PostMapping("/naver/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        naverUserService.registerNaverUser(userDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

}
