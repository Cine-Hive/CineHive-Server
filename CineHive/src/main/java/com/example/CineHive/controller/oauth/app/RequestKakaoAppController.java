package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.KakaoUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.service.UserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
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
            // 엑세스 토큰으로 사용자 정보 가져오기 (앱에서 SDK로 사용자 인증 처리까지 한 후, 액세스 토큰을 서버에 보내야 함.)
            // 웹은 인가 코드를 클라이언트한테서 받고, 이때 인가 코드에 대해 처리를 한 후, 엑세스 토큰을 서버에서 요청을 보내서 처리를 한다면,
            // 앱은 SDK로 사용자 인증 처리까지 한 후(인가 코드를 sdk에서 다 처리) -> 액세스 토큰까지 생성이 가능함. 이때 엑세스 토큰을 포함해서 다음과 같은 요청주소로 보내주면 됨.
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

            // DB에서 사용자 검색
            Optional<User> user = userRepository.findByMemEmail(userInfo.getMemEmail());

            // 사용자가 있다면 JWT를 생성하고 위 userinfo를 json 형식으로 body에 담아서 응담
            if (user != null) {
                // 기존 사용자: JWT 토큰 생성 및 반환
                String jwtToken = jwtUtil.generateToken(userInfo.getMemEmail());
                return ResponseEntity.ok(Map.of("token", jwtToken, "user", userInfo));
            } else {
                // 아니라면 신규 사용자를 등록하도록
                return ResponseEntity.status(201).body(userInfo); // 추가 정보 입력을 위한 응답은 201로 던지는데, 클라이언트 측에서 201 응답의 조건을 걸어 페이지 반환처리 하면 됨
            }
        } catch (Exception e) {
            log.error("Error during Kakao login", e);
            return ResponseEntity.status(500).body("로그인 처리 중 오류 발생");
        }
    }

    @Operation(summary = "카카오 사용자 회원가입", description = "카카오 로그인 후, 사용자가 추가 정보를 입력하면 이를 기반으로 사용자 정보를 저장")
    @PostMapping("/kakao/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
