package com.example.CineHive.controller.user;

import com.example.CineHive.dto.user.MemNameRequest;
import com.example.CineHive.dto.user.MemSexRequest;
import com.example.CineHive.dto.user.MemPasswordRequest;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.board.*;
import com.example.CineHive.repository.reply.*;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.service.user.UserService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/myInfo")
@RequiredArgsConstructor
@Tag(name = "MyInfo Controller", description = "사용자 정보 관련 기능을 제공하는 API")
public class MyInfoController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ReplyLikeRepository replyLikeRepository;
    private final ReplyDisLikeRepository replyDislikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final DisLikeRepository disLikeRepository;
    private final LikeRepository likeRepository;
    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final ReplyBookmarkRepository replyBookmarkRepository;

    @GetMapping("/info")
    @Operation(summary = "내 정보 조회", description = "JWT를 통해 인증된 사용자의 기본 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "JWT 토큰이 없거나 유효하지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> getMyInfo(
            @Parameter(hidden = true) HttpServletRequest request) {

        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            Optional<User> optionalUser = userRepository.findByMemEmail(memEmail);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
            }

            User user = optionalUser.get();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("memName", user.getMemName());
            userInfo.put("memEmail", user.getMemEmail());
            userInfo.put("memNickname", user.getMemNickname());
            userInfo.put("memSex", user.getMemSex());
            userInfo.put("genres", user.getGenres());

            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 조회 실패");
        }
    }

    @PutMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "기존 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "기존 비밀번호 불일치"),
            @ApiResponse(responseCode = "401", description = "토큰 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> changePassword(
            @RequestBody MemPasswordRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        String token = jwtTokenUtil.extractTokenFromRequest(httpRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            boolean result = userService.changePassword(email, request.getOldPassword(), request.getNewPassword());
            return result
                    ? ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.")
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("기존 비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 중 오류 발생");
        }
    }

    @PostMapping("/check-password")
    @Operation(summary = "비밀번호 확인", description = "입력된 비밀번호가 현재 비밀번호와 일치하는지 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검증 성공"),
            @ApiResponse(responseCode = "401", description = "토큰 없음")
    })
    public ResponseEntity<?> checkPassword(
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true) HttpServletRequest servletRequest) {

        String token = jwtTokenUtil.extractTokenFromRequest(servletRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        String password = request.get("password");
        String email = jwtTokenUtil.extractUsername(token);
        boolean isMatch = userService.checkPassword(email, password);
        return ResponseEntity.ok(Collections.singletonMap("success", isMatch));
    }

    @PutMapping("/change-memname")
    @Operation(summary = "이름 변경", description = "사용자의 이름을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이름 변경 성공"),
            @ApiResponse(responseCode = "400", description = "이름 변경 실패"),
            @ApiResponse(responseCode = "401", description = "토큰 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> changeMemName(
            @RequestBody MemNameRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        String token = jwtTokenUtil.extractTokenFromRequest(httpRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            boolean result = userService.changeMemName(email, request.getNewMemName());
            return result
                    ? ResponseEntity.ok("이름이 성공적으로 변경되었습니다.")
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이름 변경 실패");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이름 변경 중 오류 발생");
        }
    }

    @PutMapping("/change-memsex")
    @Operation(summary = "성별 변경", description = "사용자의 성별을 변경합니다. (male, female, other 허용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성별 변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 성별"),
            @ApiResponse(responseCode = "401", description = "토큰 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> changeMemSex(
            @RequestBody MemSexRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        String token = jwtTokenUtil.extractTokenFromRequest(httpRequest);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String email = jwtTokenUtil.extractUsername(token);
            boolean result = userService.changeMemSex(email, request.getNewMemSex());
            return result
                    ? ResponseEntity.ok("성별이 성공적으로 변경되었습니다.")
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효한 성별 값은 male, female, other 입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("성별 변경 중 오류 발생");
        }
    }

    @DeleteMapping("/delete-account")
    @Operation(summary = "회원 탈퇴", description = "회원 정보를 포함한 모든 데이터를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 완료"),
            @ApiResponse(responseCode = "401", description = "토큰 없음"),
            @ApiResponse(responseCode = "500", description = "회원 탈퇴 실패")
    })
    @Transactional
    public ResponseEntity<?> deleteAccount(@Parameter(hidden = true) HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);

            replyLikeRepository.deleteByMemEmail(memEmail);
            replyDislikeRepository.deleteByMemEmail(memEmail);
            bookmarkRepository.deleteByUser_MemEmail(memEmail);
            replyBookmarkRepository.deleteByMemEmail(memEmail);
            likeRepository.deleteByUser_MemEmail(memEmail);
            disLikeRepository.deleteByUser_MemEmail(memEmail);
            commentRepository.deleteByUser_MemEmail(memEmail);
            replyRepository.deleteByMemEmail(memEmail);
            boardRepository.deleteByUserMemEmail(memEmail);
            userRepository.deleteByMemEmail(memEmail);

            return ResponseEntity.ok("회원 탈퇴 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 실패");
        }
    }
}
