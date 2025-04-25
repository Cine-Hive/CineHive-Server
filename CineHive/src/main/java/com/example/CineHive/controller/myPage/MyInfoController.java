package com.example.CineHive.controller.myPage;

import com.example.CineHive.dto.user.ChangeMemNameRequest;
import com.example.CineHive.dto.user.ChangeMemSexRequest;
import com.example.CineHive.dto.user.ChangePasswordRequest;
import com.example.CineHive.service.UserService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/myInfo")
@RequiredArgsConstructor
@Tag(name = "MyInfo Controller", description = "사용자 정보 관련 기능을 제공하는 API")
public class MyInfoController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @PutMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "기존 비밀번호 확인 후 새 비밀번호로 변경")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        String token = jwtTokenUtil.extractTokenFromRequest(httpRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            boolean result = userService.changePassword(email, request.getOldPassword(), request.getNewPassword());
            if (result) {
                return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("기존 비밀번호가 일치하지 않습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 중 오류 발생");
        }
    }


    @PutMapping("/change-memname")
    @Operation(summary = "이름 변경", description = "사용자가 입력한 이름으로 변경")
    public ResponseEntity<?> changeMemName(@RequestBody ChangeMemNameRequest request, HttpServletRequest httpRequest) {
        String token = jwtTokenUtil.extractTokenFromRequest(httpRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            boolean result = userService.changeMemName(email, request.getNewMemName());
            if (result) {
                return ResponseEntity.ok("이름이 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이름 변경 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이름 변경 중 오류 발생");
        }
    }


    @PutMapping("/change-memsex")
    @Operation(summary = "성별 변경", description = "사용자가 입력한 성별로 변경 (male, female, other만 허용)")
    public ResponseEntity<?> changeMemSex(@RequestBody ChangeMemSexRequest request, HttpServletRequest httpRequest) {
        String token = jwtTokenUtil.extractTokenFromRequest(httpRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            boolean result = userService.changeMemSex(email, request.getNewMemSex());
            if (result) {
                return ResponseEntity.ok("성별이 성공적으로 변경되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효한 성별 값은 male, female, other 입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("성별 변경 중 오류 발생");
        }
    }
}
