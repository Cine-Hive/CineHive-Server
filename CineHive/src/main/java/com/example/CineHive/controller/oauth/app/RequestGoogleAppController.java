package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.GoogleUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.GoogleUserService;
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

@Tag(name = "Google User App Controller", description = "구글 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RequestGoogleAppController {
    @Autowired
    private GoogleUserService googleUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "구글 앱 로그인", description = "앱에서 SDK를 실행 후 인증 및 로그인이 성공된 후, 앱에서 Access Token을 담아서 요청을 보내면 서버에서 json 데이터를 클라이언트에게 보내야 할 요청코드 ")
    @PostMapping("/google/app-login")
    public ResponseEntity<?> kakaoAppLogin(@RequestBody String accessToken) {
        try {
            GoogleUserInfo userInfo = googleUserService.getUserInfo(accessToken);
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


    @Operation(summary = "구글 사용자 회원가입", description = "구글 로그인 후, 사용자가 추가 정보를 입력하면 이를 기반으로 사용자 정보를 저장")
    @PostMapping("/google/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        googleUserService.registerGoogleUser(userDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
