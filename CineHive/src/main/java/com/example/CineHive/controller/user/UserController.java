package com.example.CineHive.controller.user;

import com.example.CineHive.dto.user.LoginDto;
import com.example.CineHive.dto.user.UserDto;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.user.UserService;
import com.example.CineHive.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag(name = "User Controller", description = "사용자 관련 기능을 제공하는 API")
@Slf4j
@RequiredArgsConstructor
@RestController
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(
            summary = "회원가입",
            description = "사용자 정보를 입력받아 일반 회원가입을 진행, 중복 검사 통과 후 user 테이블에 저장"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 회원가입했습니다!"), // 201 Created 응답 추가
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 이미 등록된 이메일/닉네임 또는 기타 유효성 오류"), // 400 Bad Request 응답 추가
            @ApiResponse(responseCode = "500", description = "서버 오류 발생") // 500 Internal Server Error 응답 추가
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserDto userDto) {
        try {

            if (!userRepository.findByMemEmail(userDto.getMemEmail()).isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "이미 등록된 이메일입니다."));
            }


            if (!userRepository.findByMemNickname(userDto.getMemNickname()).isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "이미 등록된 닉네임입니다."));
            }

            boolean isRegistered = userService.registerUser(userDto);

            if (isRegistered) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "성공적으로 회원가입했습니다!");
                return ResponseEntity.status(201).body(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "회원가입 실패. 다시 시도해 주세요!");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("회원가입 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    @Operation(
            summary = "로그인",
            description = "user 테이블에 사용자가 입력한 ID와 비밀번호 쌍이 맞는지 확인 후 로그인"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공, JWT 토큰 및 사용자 정보 반환"), // 200 OK 응답 추가
            @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일/비밀번호)"), // 401 Unauthorized 응답 추가 (로그인 실패 시)
            @ApiResponse(responseCode = "500", description = "서버 오류 발생") // 500 Internal Server Error 응답 추가
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto loginRequest, HttpServletRequest request) {
        try {
            userService.loginUser(loginRequest.getMemEmail(), loginRequest.getMemPassword(), request); // 로그인 기록은 저장되지만 반환 X

            User user = userService.getUserInfo(loginRequest.getMemEmail());
            String token = jwtUtil.generateToken(user.getMemEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "로그인 성공");
            response.put("user", new HashMap<String, Object>() {{
                put("gender", user.getMemSex());
                put("email", user.getMemEmail());
                put("name", user.getMemName());
                put("nickname", user.getMemNickname());
                put("genres", user.getGenres());
            }});

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(
            summary = "닉네임 중복 확인",
            description = "user 테이블에 해당 닉네임이 이미 등록되어 있는지 확인"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 사용 가능 여부 반환 (true: 사용 가능, false: 사용 불가)"), // 200 OK 응답 추가
            @ApiResponse(responseCode = "500", description = "서버 오류 발생") // 500 Internal Server Error 응답 추가 (예외 발생 시)
    })
    @GetMapping("/checknickname/{memNickname}")
    public ResponseEntity<Boolean> checkmemNickname(@PathVariable(value="memNickname") String memNickname) {
        Optional<User> existingUser = userRepository.findByMemNickname(memNickname);
        boolean isAvailable = existingUser.isEmpty(); // 사용자 ID가 존재하지 않으면 사용 가능
        return ResponseEntity.ok(isAvailable);
    }

    @Operation(
            summary = "이메일 중복 확인",
            description = "user 테이블에 해당 이메일이 이미 등록되어 있는지 확인"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 사용 가능 여부 반환 (true: 사용 가능, false: 사용 불가)"), // 200 OK 응답 추가
            @ApiResponse(responseCode = "500", description = "서버 오류 발생") // 500 Internal Server Error 응답 추가 (예외 발생 시)
    })
    @GetMapping("/checkemail/{memEmail}")
    public ResponseEntity<Boolean> checkmemEmail(@PathVariable(value="memEmail") String memEmail) {
        Optional<User> existingUser = userRepository.findByMemEmail(memEmail);
        boolean isAvailable = existingUser.isEmpty(); // 사용자 ID가 존재하지 않으면 사용 가능
        return ResponseEntity.ok(isAvailable);
    }
}