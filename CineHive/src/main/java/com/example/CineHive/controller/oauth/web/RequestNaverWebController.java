package com.example.CineHive.controller.oauth.web;

import com.example.CineHive.dto.oauth.NaverUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.oauth.NaverUser;
import com.example.CineHive.repository.NaverUserRepository;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.NaverUserService;
import com.example.CineHive.service.UserService;
import com.example.CineHive.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Naver User Controller", description = "네이버 로그인 API 관련 기능을 제공하는 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class RequestNaverWebController {

    @Autowired
    private final NaverUserService naverUserService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final NaverUserRepository naverUserRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary ="네이버 로그인 리다이렉션", description = "사용자를 네이버 OAuth 로그인 페이지로 리다이렉션하여 네이버 인증을 시작")
    @GetMapping("/naver")
    public void naverLoginRedirect(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + naverUserService.getClientId() +
                "&redirect_uri=" + URLEncoder.encode("http://localhost:8081/api/auth/naver/callback", "UTF-8") +
                "&state=" + UUID.randomUUID().toString() +
                "&scope=name,email,nickname"; // 필요한 스코프 추가
        response.sendRedirect(redirectUrl);
    }


    @Operation(summary = "네이버 OAuth 로그인 및 사용자 등록", description = "네이버 OAuth 인증 후 사용자 정보를 이용하여 로그인하거나, 신규 사용자를 등록하고 로그인 후 사용자를 리다이렉션")
    @GetMapping("/naver/callback")
    public void naverCallback(@RequestParam String code, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = naverUserService.getAccessToken(code);
            NaverUserInfo userInfo = naverUserService.getUserInfo(accessToken);

            User user = userRepository.findByMemEmail(userInfo.getMemEmail()).orElse(null);


            HttpSession session = request.getSession();
            session.setAttribute("user", userInfo);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            if (userService.checkUserExists(userInfo.getMemEmail())) {
                String jwtToken = userService.generateJwtToken(userInfo.getMemEmail());
                session.setAttribute("jwtToken", jwtToken);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("jwt", jwtToken);
                responseData.put("user", userInfo);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(new ObjectMapper().writeValueAsString(responseData));
                response.sendRedirect("http://localhost:8080/");
            } else {
                session.setAttribute("memEmail", userInfo.getMemEmail());
                session.setAttribute("nickname", userInfo.getMemNickname());
                response.sendRedirect("http://localhost:8080/additional-info?loginType=naver");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during Naver login process");
        }
    }

    @Operation(summary = "네이버 로그인 성공 페이지", description = "네이버 로그인 성공 시 사용자 정보를 반환")
    @GetMapping("/naver/success")
    public ResponseEntity<?> naverSuccessPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            NaverUserInfo userInfo = (NaverUserInfo) session.getAttribute("user");
            if (userInfo != null) {
                return ResponseEntity.ok(userInfo);
            }
        }
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @Operation(summary = "네이버 사용자 등록", description = "네이버 사용자 정보를 입력받아 회원가입을 진행, 중복 검사 통과 후 naver_user 테이블에 저장")
    @PostMapping("/naver/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        naverUserService.registerNaverUser(userDto);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "네이버 로그인 성공 정보 반환", description = "세션에서 네이버 로그인한 사용자 정보를 가져와 반환, 인증되지 않은 사용자는 401 오류를 반환")
    @GetMapping("/naver/login/success")
    public ResponseEntity<?> loginSuccessPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            NaverUserInfo userInfo = (NaverUserInfo) session.getAttribute("user");

            if (userInfo != null) {
                String token = jwtUtil.generateToken(userInfo.getMemEmail());

                Map<String, Object> response = new HashMap<>();
                response.put("userInfo", userInfo);
                response.put("token", token);

                log.info("Response Data: {}", response);
                return ResponseEntity.ok(response);
            }
        }
        log.warn("Unauthorized access attempt");
        return ResponseEntity.status(401).body("Unauthorized");
    }
}