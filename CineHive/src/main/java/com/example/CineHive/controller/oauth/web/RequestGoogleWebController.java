package com.example.CineHive.controller.oauth.web;

import com.example.CineHive.dto.oauth.google.GoogleUserInfo;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.oauth.GoogleUserService;
import com.example.CineHive.service.user.UserService;
import com.example.CineHive.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Google User Controller", description = "구글 로그인 API 관련 기능을 제공하는 API")
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class RequestGoogleWebController {

    @Autowired
    private GoogleUserService googleUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "구글 로그인 리다이렉션", description = "사용자를 구글 OAuth 로그인 페이지로 리다이렉션하여 구글 인증을 시작")
    @GetMapping("/google")
    public void googleLoginRedirect(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + googleUserService.getClientId() +
                "&redirect_uri=" + URLEncoder.encode(googleUserService.getRedirectUri(), "UTF-8") +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("email profile", "UTF-8"); // 'scope' 값 인코딩 추가

        System.out.println("Redirect URL: " + redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    @Operation(summary = "구글 OAuth 로그인 및 사용자 등록", description = "구글 OAuth 인증 후 구글 사용자 정보를 이용하여 로그인하거나 신규 사용자를 등록, 인증 후 해당 사용자를 리다이렉션")
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = googleUserService.getAccessToken(code);
            GoogleUserInfo userInfo = googleUserService.getUserInfo(accessToken);

            User user = userRepository.findByMemEmail(userInfo.getMemEmail()).orElse(null);

            HttpSession session = request.getSession();
            session.setAttribute("user", userInfo);

            if (user != null) {
                userInfo.setGenres(user.getGenres());
                userInfo.setMemName(user.getMemName());
            }

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
                response.sendRedirect("http://localhost:8080/additional-info?loginType=google"); // 신규 사용자 리다이렉트
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during google login process");
        }
    }

    @Operation(summary = "구글 인증 성공", description = "구글 OAuth 인증 성공 후 세션에 저장된 사용자 정보를 반환, 사용자가 인증되지 않은 경우 401 상태 코드와 함께 오류 메시지를 반환")
    @GetMapping("/google/success")
    public ResponseEntity<?> googleSuccessPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            GoogleUserInfo userInfo = (GoogleUserInfo) session.getAttribute("user");
            log.info("User info in session: {}", userInfo);

            if (userInfo != null) {
                return ResponseEntity.ok(userInfo);
            }
        }
        log.warn("Unauthorized access attempt");
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @Operation(summary = "구글 로그인 성공 정보 반환", description = "세션에서 구글 로그인한 사용자 정보를 가져와 반환, 인증되지 않은 사용자는 401 오류를 반환")
    @GetMapping("/google/login/success")
    public ResponseEntity<?> loginSuccessPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        log.info("Session exists: {}", session != null);

        if (session != null) {
            GoogleUserInfo userInfo = (GoogleUserInfo) session.getAttribute("user");
            log.info("User info in session: {}", userInfo);

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
