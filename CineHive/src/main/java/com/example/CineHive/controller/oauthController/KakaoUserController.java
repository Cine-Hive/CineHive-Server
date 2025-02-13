package com.example.CineHive.controller.oauthController;

import com.example.CineHive.dto.oauth.KakaoUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.oauth.GoogleUser;
import com.example.CineHive.entity.oauth.KakaoUser;
import com.example.CineHive.repository.KakaoUserRepository;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.time.LocalDateTime;

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

    @GetMapping("/kakao")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String url = "https://kauth.kakao.com/oauth/authorize?client_id=" + kakaoUserService.getClientId() +
                "&redirect_uri=" + kakaoUserService.getRedirectUri() + "&response_type=code";
        response.sendRedirect(url);
    }

    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = kakaoUserService.getAccessToken(code);
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

            KakaoUser kakaoUser = kakaoUserRepository.findByKakaoId(userInfo.getKakaoId()).orElse(null);

            if (kakaoUser == null) {
                System.out.println("GoogleUser is null for Google ID: " + userInfo.getKakaoId());
                kakaoUser = kakaoUserService.registerNewKakaoUser(userInfo);
            } else {
                System.out.println("GoogleUser found: " + kakaoUser.getName() + ", " + kakaoUser.getGenres());
            }

            userInfo.setName(kakaoUser.getName());
            userInfo.setGenres(kakaoUser.getGenres());

            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(userInfo));

            // 사용자 존재 여부 확인
            if (userService.checkUserExists(userInfo.getKakaoId())) {
                // 기존 회원인 경우
                HttpSession session = request.getSession();
                session.setAttribute("user", userInfo);
                response.sendRedirect("http://localhost:8080/");
            } else {

                kakaoUserService.registerUser(userInfo);
                HttpSession session = request.getSession();
                session.setAttribute("user", userInfo);
                response.sendRedirect("http://localhost:8080/additional-info?loginType=kakao");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during Kakao login process");
        }
    }


    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestBody KakaoUserInfo userInfo, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("user", userInfo);
        return ResponseEntity.ok("Session created successfully");
    }


    @GetMapping("/kakao/success")
    public ResponseEntity<?> successPage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        log.info("Session exists: {}", session != null);

        if (session != null) {
            KakaoUserInfo userInfo = (KakaoUserInfo) session.getAttribute("user");
            log.info("User info in session: {}", userInfo);

            if (userInfo != null) {
                return ResponseEntity.ok(userInfo);
            }
        }
        log.warn("Unauthorized access attempt"); // 인증 실패 로그
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 카카오 로그아웃 URL 생성
        String logoutUrl = "https://kauth.kakao.com/oauth/logout?client_id=" + kakaoUserService.getClientId() + "&logout_redirect_uri=" + kakaoUserService.getLogoutRedirectUri();

        return ResponseEntity.ok(logoutUrl);
    }

    @GetMapping("/logout")
    public RedirectView handleLogoutRedirect(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:8080/login");
        return redirectView;
    }

    @GetMapping("/kakao/check-user")
    public ResponseEntity<Boolean> checkUser(@RequestParam String kakaoId) {
        boolean exists = userService.checkUserExists(kakaoId);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/kakao/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        User newUser = new User();
        newUser.setMemUserid(userDto.getMemUserid());
        newUser.setMemEmail(userDto.getMemEmail());
        newUser.setMemPw(userDto.getMemPassword());
        newUser.setMemNickname(userDto.getMemNickname());
        newUser.setMemName(userDto.getMemName());
        newUser.setMemPhone(userDto.getMemPhone());
        newUser.setMemSex(userDto.getMemSex());
        newUser.setKakaoId(userDto.getKakaoId());
        newUser.setMemRegisterDatetime(LocalDateTime.now());
        newUser.setMemType("카카오");
        newUser.setGenres(userDto.getGenres());

        userRepository.save(newUser);

        KakaoUser kakaoUser = kakaoUserRepository.findByKakaoId(userDto.getKakaoId())
                .orElseThrow(() -> new IllegalArgumentException("Kakao User not found"));
        kakaoUser.setMemUserId(userDto.getMemEmail());
        kakaoUser.setName(userDto.getMemName());
        kakaoUser.setGenres(userDto.getGenres());
        kakaoUserRepository.save(kakaoUser);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }


}