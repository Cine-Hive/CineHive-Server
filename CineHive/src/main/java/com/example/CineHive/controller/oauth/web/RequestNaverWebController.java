package com.example.CineHive.controller.oauth.web;

import com.example.CineHive.dto.oauth.naver.NaverUserInfo;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.oauth.NaverUserService;
import com.example.CineHive.service.user.UserService;
import com.example.CineHive.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private JwtUtil jwtUtil;


    @GetMapping("/naver")
    @Operation(summary ="네이버 로그인 리다이렉션", description = "사용자를 네이버 OAuth 로그인 페이지로 리다이렉션하여 네이버 인증을 시작")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "네이버 OAuth 로그인 페이지로 리다이렉션"),
            @ApiResponse(responseCode = "500", description = "리다이렉션 URL 생성 중 오류 발생")
    })
    public void naverLoginRedirect(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + naverUserService.getClientId() +
                "&redirect_uri=" + URLEncoder.encode("http://localhost:8081/api/auth/naver/callback", "UTF-8") +
                "&state=" + UUID.randomUUID().toString() +
                "&scope=name,email,nickname"; // 필요한 스코프 추가
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/naver/callback")
    @Operation(summary = "네이버 OAuth 로그인 및 사용자 등록", description = "네이버 OAuth 인증 후 사용자 정보를 이용하여 로그인하거나, 신규 사용자를 등록하고 로그인 후 사용자를 리다이렉션")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존 네이버 사용자 로그인 성공 후 JSON 데이터 반환 및 메인 페이지로 리다이렉션"),
            @ApiResponse(responseCode = "302", description = "신규 네이버 사용자, 추가 정보 입력 페이지로 리다이렉션"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 'code', 'state' 파라미터 누락 또는 유효하지 않음)"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Access Token 또는 네이버 API 호출 실패"),
            @ApiResponse(responseCode = "500", description = "인증 처리 중 오류 발생")
    })
    public void naverCallback(@RequestParam String code, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = naverUserService.getAccessToken(code);
            NaverUserInfo userInfo = naverUserService.getUserInfo(accessToken);

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
                response.sendRedirect("http://localhost:8080/additional-info?loginType=naver");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during Naver login process");
        }
    }

    @GetMapping("/naver/success")
    @Operation(summary = "네이버 로그인 성공 정보 반환 (세션 기반)", description = "네이버 OAuth 인증 성공 후 세션에 저장된 사용자 정보를 반환, 사용자가 인증되지 않은 경우 401 상태 코드와 함께 오류 메시지를 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증된 네이버 사용자 정보 (NaverUserInfo) 반환"),
            @ApiResponse(responseCode = "401", description = "세션에 사용자 정보가 없거나 유효하지 않음 (인증되지 않은 사용자)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
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

    @GetMapping("/naver/login/success")
    @Operation(summary = "네이버 로그인 성공 정보 반환 (세션 & 토큰 포함)", description = "세션에서 네이버 로그인한 사용자 정보를 가져와 JWT 토큰과 함께 반환, 인증되지 않은 사용자는 401 오류를 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증된 네이버 사용자 정보 (NaverUserInfo) 및 JWT 토큰 반환"),
            @ApiResponse(responseCode = "401", description = "세션에 사용자 정보가 없거나 유효하지 않음 (인증되지 않은 사용자)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
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