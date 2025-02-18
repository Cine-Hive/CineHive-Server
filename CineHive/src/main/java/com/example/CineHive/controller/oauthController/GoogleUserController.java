package com.example.CineHive.controller.oauthController;

import com.example.CineHive.dto.oauth.GoogleUserInfo;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.oauth.GoogleUser;
import com.example.CineHive.repository.GoogleUserRepository;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.service.oauth.GoogleUserService;
import com.example.CineHive.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class GoogleUserController {

    @Autowired
    private GoogleUserService googleUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleUserRepository googleUserRepository;

    @Operation(summary ="구글 로그인 리다이렉션", description = "사용자를 구글 OAuth 로그인 페이지로 리다이렉션하여 구글 인증을 시작")
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
    @Operation(summary = "구글 OAuth 로그린 및 사용자 등록", description = "구글 OAuth 인증 후 구글 사용자 정보를 이용하여 로그인하거나 신규 사용자를 등록, 인증 후 해당 사용자를 리다이렉션")
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accessToken = googleUserService.getAccessToken(code);
            GoogleUserInfo userInfo = googleUserService.getUserInfo(accessToken);


            GoogleUser googleUser = googleUserRepository.findByGoogleId(userInfo.getGoogleId()).orElse(null);

            if (googleUser == null) {
                System.out.println("GoogleUser is null for Google ID: " + userInfo.getGoogleId());
                googleUser = googleUserService.registerNewGoogleUser(userInfo);  // 예: 구글 사용자 등록 메서드
            } else {
                System.out.println("GoogleUser found: " + googleUser.getName() + ", " + googleUser.getGenres());
            }

            userInfo.setMemName(googleUser.getName());
            userInfo.setGenres(googleUser.getGenres());

            response.setContentType("application/json");
            response.getWriter().write(new ObjectMapper().writeValueAsString(userInfo));

            if (!userService.checkUserExistsGoogle(userInfo.getGoogleId())) {
                googleUserService.registerUser(userInfo);
                HttpSession session = request.getSession();
                session.setAttribute("user", userInfo);
                response.sendRedirect("http://localhost:8080/additional-info?loginType=google");
            } else {

                HttpSession session = request.getSession();
                session.setAttribute("user", userInfo);
                response.sendRedirect("http://localhost:8080/");
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
            if (userInfo != null) {
                return ResponseEntity.ok(userInfo);
            }
        }
        return ResponseEntity.status(401).body("Unauthorized");
    }

    @Operation(summary = "구글 회원가입", description = "사용자가 제공한 정보로 회원가입하고 google_user 테이블에 저장")
    @PostMapping("/google/register")
    public ResponseEntity<String> registerUserDetails(@RequestBody UserDto userDto) {
        User newUser = new User();
        newUser.setMemEmail(userDto.getMemEmail());
        newUser.setMemPw(userDto.getMemPassword());
        newUser.setMemNickname(userDto.getMemNickname());
        newUser.setMemName(userDto.getMemName());
        newUser.setMemSex(userDto.getMemSex());
        newUser.setGoogleId(userDto.getGoogleId());
        newUser.setMemRegisterDatetime(LocalDateTime.now());
        newUser.setMemType("구글");
        newUser.setGenres(userDto.getGenres());
        userRepository.save(newUser);

        GoogleUser googleUser = googleUserRepository.findByGoogleId(userDto.getGoogleId())
                .orElseThrow(() -> new IllegalArgumentException("Google User not found"));
        googleUser.setName(userDto.getMemName());  // 이름 업데이트
        googleUser.setGenres(userDto.getGenres());  // 장르 업데이트
        googleUserRepository.save(googleUser);

        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "구글 사용자 중복 확인", description = "해당 구글 ID가 이미 등록되어 있는지 확인")
    @GetMapping("/google/check-user")
    public ResponseEntity<Boolean> checkUser(@RequestParam String googleId) {
        boolean exists = userService.checkUserExistsGoogle(googleId);
        return ResponseEntity.ok(exists);
    }

}