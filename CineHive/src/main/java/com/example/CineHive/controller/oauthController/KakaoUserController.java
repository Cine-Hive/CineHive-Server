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
import io.swagger.v3.oas.annotations.Operation;
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
                System.out.println("GoogleUser is null for Google ID: " + userInfo.getMemEmail());
                kakaoUser = kakaoUserService.registerNewKakaoUser(userInfo);
            } else {
                System.out.println("GoogleUser found: " + kakaoUser.getName() + ", " + kakaoUser.getGenres());
            }

            userInfo.setMemName(kakaoUser.getName());
            userInfo.setGenres(kakaoUser.getGenres());

            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(userInfo));

            // 사용자 존재 여부 확인
            if (userService.checkUserExists(userInfo.getMemEmail())) {
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

    @Operation(summary = "세션 생성", description = "카카오 로그인 후 인증된 사용자의 정보를 세션에 저장")
    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestBody KakaoUserInfo userInfo, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("user", userInfo);
        return ResponseEntity.ok("Session created successfully");
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
                return ResponseEntity.ok(userInfo);
            }
        }
        log.warn("Unauthorized access attempt"); // 인증 실패 로그
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @Operation(summary = "카카오 로그아웃", description = "카카오 로그아웃을 위한 URL을 반환, 클라이언트에서 이 URL을 호출하여 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 카카오 로그아웃 URL 생성
        String logoutUrl = "https://kauth.kakao.com/oauth/logout?client_id=" + kakaoUserService.getClientId() + "&logout_redirect_uri=" + kakaoUserService.getLogoutRedirectUri();

        return ResponseEntity.ok(logoutUrl);
    }

    @Operation(summary = "로그아웃 후 리다이렉션", description = "로그아웃 후 클라이언트를 로그인 페이지로 리다이렉션")
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