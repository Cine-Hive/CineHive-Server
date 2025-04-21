package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.kakao.KakaoTokenRequest;
import com.example.CineHive.dto.oauth.kakao.KakaoUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/*
    엑세스 토큰으로 사용자 정보 가져오기 (앱에서 SDK로 사용자 인증 처리까지 한 후, 액세스 토큰을 서버에 보내야 함.)
    웹은 인가 코드를 클라이언트한테서 받고, 이때 인가 코드에 대해 처리를 한 후, 엑세스 토큰을 서버에서 요청을 보내서 처리를 한다면,
    앱은 SDK로 사용자 인증 처리까지 한 후(인가 코드를 sdk에서 다 처리) -> 액세스 토큰까지 생성이 가능함. 이때 엑세스 토큰을 포함해서 다음과 같은 요청주소로 보내주면 됨.
 */
@Tag(name = "Kakao User App Controller", description = "카카오 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RequestKakaoAppController {

    @Autowired
    private KakaoUserService kakaoUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/kakao/app-login")
    public ResponseEntity<?> kakaoAppLogin(@RequestBody KakaoTokenRequest kakaoTokenRequest) {
        try {
            String accessToken = kakaoTokenRequest.getAccessToken();
            log.info("[Kakao Login] accessToken 수신: {}", accessToken);

            // 카카오에서 사용자 정보 가져오기
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);
            log.info("[Kakao Login] Kakao 사용자 정보 수신: {}", userInfo);

            Optional<User> user = userRepository.findByMemEmail(userInfo.getMemEmail());

            if (user.isPresent()) {

                String jwtToken = jwtUtil.generateToken(userInfo.getMemEmail());
                // 토큰 값 또한 필요에 따라 로깅 필요
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("memEmail", userInfo.getMemEmail());
                userMap.put("memNickname", userInfo.getMemNickname());
                userMap.put("memName", userInfo.getMemName());
                userMap.put("genres", userInfo.getGenres());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("token", jwtToken);
                responseMap.put("user", userMap);

                return ResponseEntity.ok(responseMap);
            } else {
                log.info("[Kakao Login] 신규 회원, 회원가입 필요: {}", userInfo.getMemEmail());
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("memEmail", userInfo.getMemEmail());
                userMap.put("memNickname", userInfo.getMemNickname());
                userMap.put("memName", userInfo.getMemName());
                userMap.put("genres", userInfo.getGenres());

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("user", userMap);

                return ResponseEntity.status(201).body(responseMap);
            }

        } catch (Exception e) {
            log.error("[Kakao Login] 로그인 처리 중 예외 발생", e);
            return ResponseEntity.status(500).body("로그인 처리 중 오류 발생");
        }
    }



    @Operation(summary = "카카오 사용자 회원가입", description = "카카오 로그인 후, 사용자가 추가 정보를 입력하면 이를 기반으로 사용자 정보를 저장")
    @PostMapping("/kakao/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        kakaoUserService.registerKakaoUser(userDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
