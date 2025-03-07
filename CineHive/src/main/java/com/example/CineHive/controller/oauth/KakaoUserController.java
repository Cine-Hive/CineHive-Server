package com.example.CineHive.controller.oauth;

import com.example.CineHive.dto.oauth.JwtResponse;
import com.example.CineHive.dto.oauth.KakaoUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.oauth.KakaoUser;
import com.example.CineHive.repository.KakaoUserRepository;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.service.UserService;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Kakao User Controller", description = "카카오 로그인 API 관련 기능을 제공하는 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class KakaoUserController {

    @Autowired
    private KakaoUserService kakaoUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private KakaoUserRepository kakaoUserRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "카카오 로그인", description = "카카오 OAuth 로그인 페이지로 사용자를 리다이렉션하여 카카오 인증을 시작")
    @GetMapping("/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String url = "https://kauth.kakao.com/oauth/authorize?client_id=" + kakaoUserService.getClientId() +
                "&redirect_uri=" + kakaoUserService.getRedirectUri() + "&response_type=code";
        response.sendRedirect(url);
    }

    @Operation(summary = "카카오 OAuth 로그인 및 사용자 등록", description = "카카오 OAuth 인증 후 사용자 정보를 이용하여 로그인하거나, 신규 사용자를 등록하고 로그인 후 사용자를 리다이렉션")
    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = kakaoUserService.getAccessToken(code);
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

            KakaoUser kakaoUser = kakaoUserRepository.findByMemEmail(userInfo.getMemEmail()).orElse(null);

            if (kakaoUser == null) {
                System.out.println("KakaoUser is null for email: " + userInfo.getMemEmail());
                kakaoUser = kakaoUserService.registerNewKakaoUser(userInfo);
            } else {
                System.out.println("KakaoUser found: " + kakaoUser.getName() + ", " + kakaoUser.getGenres());
            }

            userInfo.setMemName(kakaoUser.getName());
            userInfo.setGenres(kakaoUser.getGenres());


            String token = jwtUtil.generateToken(userInfo.getMemEmail());


            if (userService.checkUserExists(userInfo.getMemEmail())) {

                HttpSession session = request.getSession();
                session.setAttribute("user", userInfo);


                response.setContentType("application/json");
                response.getWriter().write(new ObjectMapper().writeValueAsString(new JwtResponse(token, userInfo)));
                response.sendRedirect("http://localhost:8080/");
            } else {

                kakaoUserService.registerUser(userInfo);
                HttpSession session = request.getSession();
                session.setAttribute("user", userInfo);

                log.info("Response Data: {}", response);
                response.setContentType("application/json");
                response.getWriter().write(new ObjectMapper().writeValueAsString(new JwtResponse(token, userInfo)));
                response.sendRedirect("http://localhost:8080/additional-info?loginType=kakao");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "카카오 로그인 과정 중 오류 발생");
        }
    }

    @Operation(summary = "카카오 로그인 성공 정보 반환", description = "세션에서 카카오 로그인한 사용자 정보를 가져와 반환, 인증되지 않은 사용자는 401 오류를 반환")
    @GetMapping("/kakao/success")
    public ResponseEntity<?> successPage(HttpServletRequest request) {
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


    @Operation(summary = "카카오 사용자 중복 확인", description = "카카오 사용자 ID를 이용하여 사용자가 이미 존재하는지 확인")
    @GetMapping("/kakao/check-user")
    public ResponseEntity<Boolean> checkUser(@RequestParam String kakaoId) {
        boolean exists = userService.checkUserExists(kakaoId);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "카카오 사용자 회원가입", description = "카카오 로그인 후, 사용자가 추가 정보를 입력하면 이를 기반으로  kakao_user 테이블에 저장")
    @PostMapping("/kakao/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        User newUser = new User();
        newUser.setMemEmail(userDto.getMemEmail());
        newUser.setMemPw(userDto.getMemPassword());
        newUser.setMemNickname(userDto.getMemNickname());
        newUser.setMemName(userDto.getMemName());
        newUser.setMemSex(userDto.getMemSex());
        newUser.setMemRegisterDatetime(LocalDateTime.now());
        newUser.setMemType("카카오");
        newUser.setGenres(userDto.getGenres());

        userRepository.save(newUser);

        KakaoUser kakaoUser = kakaoUserRepository.findByMemEmail(userDto.getMemEmail())
                .orElseThrow(() -> new IllegalArgumentException("Kakao User not found"));
        kakaoUser.setName(userDto.getMemName());
        kakaoUser.setGenres(userDto.getGenres());
        kakaoUserRepository.save(kakaoUser);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }


}