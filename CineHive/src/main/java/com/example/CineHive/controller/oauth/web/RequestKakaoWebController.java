package com.example.CineHive.controller.oauth.web;

import com.example.CineHive.dto.oauth.kakao.KakaoUserInfo;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.service.user.UserService;
import com.example.CineHive.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RequestKakaoWebController {

    @Autowired
    private KakaoUserService kakaoUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 OAuth 로그인 페이지로 사용자를 리다이렉션하여 카카오 인증을 시작")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "카카오 OAuth 로그인 페이지로 리다이렉션"),
            @ApiResponse(responseCode = "500", description = "리다이렉션 URL 생성 중 오류 발생")
    })
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String url = "https://kauth.kakao.com/oauth/authorize?client_id=" + kakaoUserService.getClientId() +
                "&redirect_uri=" + kakaoUserService.getRedirectUri() + "&response_type=code";
        response.sendRedirect(url);
    }

    @GetMapping("/kakao/callback")
    @Operation(summary = "카카오 OAuth 로그인 및 사용자 등록", description = "카카오 OAuth 인증 후 사용자 정보를 이용하여 로그인하거나, 신규 사용자를 등록하고 로그인 후 사용자를 리다이렉션")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존 카카오 사용자 로그인 성공 후 JSON 데이터 반환 및 메인 페이지로 리다이렉션"),
            @ApiResponse(responseCode = "302", description = "신규 카카오 사용자, 추가 정보 입력 페이지로 리다이렉션"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 'code' 파라미터 누락 또는 유효하지 않음)"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Access Token 또는 카카오 API 호출 실패"),
            @ApiResponse(responseCode = "500", description = "인증 처리 중 오류 발생")
    })
    public void kakaoCallback(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = kakaoUserService.getAccessToken(code);
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

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
            }else {
                session.setAttribute("memEmail", userInfo.getMemEmail());
                session.setAttribute("nickname", userInfo.getMemNickname());
                response.sendRedirect("http://localhost:8080/additional-info?loginType=kakao");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카카오 로그인 과정 중 오류 발생");
        }
    }

    @GetMapping("/kakao/success")
    @Operation(summary = "카카오 인증 성공 정보 반환", description = "카카오 OAuth 인증 성공 후 세션에 저장된 사용자 정보를 반환, 사용자가 인증되지 않은 경우 401 상태 코드와 함께 오류 메시지를 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증된 카카오 사용자 정보 (KakaoUserInfo) 반환"),
            @ApiResponse(responseCode = "401", description = "세션에 사용자 정보가 없거나 유효하지 않음 (인증되지 않은 사용자)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<?> successPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            KakaoUserInfo userInfo = (KakaoUserInfo) session.getAttribute("user");
            log.info("User info in session: {}", userInfo);

            if (userInfo != null) {
                return ResponseEntity.ok(userInfo);
            }
        }
        log.warn("Unauthorized access attempt");
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @GetMapping("/kakao/login/success")
    @Operation(summary = "카카오 로그인 성공 정보 반환 (토큰 포함)", description = "세션에서 카카오 로그인한 사용자 정보를 가져와 JWT 토큰과 함께 반환, 인증되지 않은 사용자는 401 오류를 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증된 카카오 사용자 정보 (KakaoUserInfo) 및 JWT 토큰 반환"),
            @ApiResponse(responseCode = "401", description = "세션에 사용자 정보가 없거나 유효하지 않음 (인증되지 않은 사용자)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<?> loginSuccessPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        log.info("Session exists: {}", session != null);

        if (session != null) {
            KakaoUserInfo userInfo = (KakaoUserInfo) session.getAttribute("user");
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