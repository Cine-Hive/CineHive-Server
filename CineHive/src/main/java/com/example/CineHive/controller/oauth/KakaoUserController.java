package com.example.CineHive.controller.oauth;

import com.example.CineHive.dto.oauth.KakaoUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.oauth.KakaoUser;
import com.example.CineHive.repository.KakaoUserRepository;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.KakaoUserService;
import com.example.CineHive.service.UserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<Map<String, Object>> kakaoCallback(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = kakaoUserService.getAccessToken(code);
            KakaoUserInfo userInfo = kakaoUserService.getUserInfo(accessToken);

            KakaoUser kakaoUser = kakaoUserRepository.findByMemEmail(userInfo.getMemEmail()).orElse(null);

            // 카카오 사용자 정보를 세션에 저장
            HttpSession session = request.getSession();
            session.setAttribute("user", userInfo);

            String token = jwtUtil.generateToken(kakaoUser.getMemEmail());



            return ResponseEntity.ok(Map.of( "userInfo", userInfo, "token",token));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error during Kakao login process");
        }
        return null;
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