package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.kakao.KakaoTokenRequest;
import com.example.CineHive.dto.oauth.kakao.KakaoUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Kakao User App Controller", description = "카카오 앱 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth/kakao")
public class RequestKakaoAppController {

    @Autowired
    private KakaoUserService kakaoUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/app-login")
    @Operation(
            summary = "카카오 앱 로그인",
            description = "앱에서 카카오 SDK 실행 후 받은 Access Token으로 사용자 정보를 가져와 로그인 처리합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존 카카오 사용자 로그인 성공, JWT 토큰 및 사용자 정보 반환"),
            @ApiResponse(responseCode = "201", description = "신규 카카오 사용자, 회원가입 추가 정보 입력 필요 알림"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 필수 파라미터 누락)"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Access Token 또는 카카오 API 호출 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
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


    @PostMapping("/register")
    @Operation(summary = "카카오 사용자 회원가입", description = "카카오 로그인 후, 사용자가 추가 정보를 입력하면 이를 기반으로 사용자 정보를 저장")
    @ApiResponses(value = { // ApiResponses 추가!
            @ApiResponse(responseCode = "200", description = "카카오 사용자 추가 정보 기반 회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 필수 정보 누락, 유효성 검사 실패, 이미 존재하는 이메일/닉네임 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        kakaoUserService.registerKakaoUser(userDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
