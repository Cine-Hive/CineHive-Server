package com.example.CineHive.controller.oauth.app;

import com.example.CineHive.dto.oauth.naver.NaverTokenRequest;
import com.example.CineHive.dto.oauth.naver.NaverUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.oauth.NaverUserService;
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


    @PostMapping("/naver/app-login")
    @Operation(summary = "네이버 앱 로그인", description = "앱에서 SDK를 실행 후 인증 및 로그인이 성공된 후, 앱에서 Access Token을 담아서 요청을 보내면 서버에서 json 데이터를 클라이언트에게 보내야 할 요청 코드")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존 네이버 사용자 로그인 성공, JWT 토큰 및 사용자 정보 반환"),
            @ApiResponse(responseCode = "201", description = "신규 네이버 사용자, 회원가입 추가 정보 입력 필요 알림"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 필수 파라미터 누락)"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Access Token 또는 네이버 API 호출 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<?> naverAppLogin(@RequestBody NaverTokenRequest naverTokenRequest) {
        try {
            String accessToken = naverTokenRequest.getAccessToken();
            log.info("[Naver Login] accessToken 수신: {}", accessToken);

            NaverUserInfo userInfo = naverUserService.getUserInfo(accessToken);
            log.info("[Naver Login] Naver 사용자 정보 수신: {}", userInfo);

            Optional<User> user = userRepository.findByMemEmail(userInfo.getMemEmail());

            if (user.isPresent()) {
                String jwtToken = jwtUtil.generateToken(userInfo.getMemEmail());

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
                log.info("[Naver Login] 신규 회원, 회원가입 필요: {}", userInfo.getMemEmail());

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
            log.error("[Naver Login] 로그인 처리 중 예외 발생", e);
            return ResponseEntity.status(500).body("로그인 처리 중 오류 발생");
        }
    }

    @PostMapping("/naver/register")
    @Operation(summary = "네이버 사용자 등록", description = "네이버 사용자 정보를 입력받아 회원가입을 진행, 중복 검사 통과 후 저장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "네이버 사용자 추가 정보 기반 회원가입 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 필수 정보 누락, 유효성 검사 실패, 이미 존재하는 이메일/닉네임 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        naverUserService.registerNaverUser(userDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
}
