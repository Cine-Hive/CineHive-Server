package com.example.CineHive.controller.myPage;

import com.example.CineHive.dto.user.ChangeMemNameRequest;
import com.example.CineHive.dto.user.ChangeMemSexRequest;
import com.example.CineHive.dto.user.ChangePasswordRequest;
import com.example.CineHive.entity.board.BoardDisLike;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.*;
import com.example.CineHive.repository.reply.ReplyBookmarkRepository;
import com.example.CineHive.repository.reply.ReplyDisLikeRepository;
import com.example.CineHive.repository.reply.ReplyLikeRepository;
import com.example.CineHive.repository.reply.ReplyRepository;
import com.example.CineHive.service.UserService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

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

    @PostMapping("/check-password")
    @Operation(summary = "비밀번호 확인", description = "비밀번호가 맞는지 검증")
    public ResponseEntity<?> checkPassword(@RequestBody Map<String, String> request, HttpServletRequest servletRequest) {
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

    @DeleteMapping("/delete-account")
    @Operation(summary = "회원 탈퇴", description = "JWT를 통해 인증된 사용자가 작성한 모든 데이터와 계정을 삭제합니다.")
    @Transactional
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);

            // 1. 감상평 좋아요 삭제
            replyLikeRepository.deleteByMemEmail(memEmail);

            // 2. 감상평 싫어요 삭제
            replyDislikeRepository.deleteByMemEmail(memEmail);

            // 3.찜 삭제
            bookmarkRepository.deleteByUser_MemEmail(memEmail);
            replyBookmarkRepository.deleteByMemEmail(memEmail);

            // 4. 게시글 좋아요 삭제
            likeRepository.deleteByUser_MemEmail(memEmail);

            // 5. 게시글 싫어요 삭제
            disLikeRepository.deleteByUser_MemEmail(memEmail);

            // 6. 댓글 삭제
            commentRepository.deleteByUser_MemEmail(memEmail);

            // 7. 감상평 삭제
            replyRepository.deleteByMemEmail(memEmail);

            // 8. 게시글 삭제
            boardRepository.deleteByUserMemEmail(memEmail);

            // 9. 유저 삭제
            userRepository.deleteByMemEmail(memEmail);

            return ResponseEntity.ok("회원 탈퇴 완료");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 실패");
        }
    }




}
