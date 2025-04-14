package com.example.CineHive.controller.myPage;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.GetListBoardDto;
import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.dto.user.ChangeMemNameRequest;
import com.example.CineHive.dto.user.ChangeMemSexRequest;
import com.example.CineHive.dto.user.ChangePasswordRequest;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;

import com.example.CineHive.entity.reply.Reply;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.LoginHistoryRepository;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.CommentRepository;
import com.example.CineHive.repository.reply.ReplyRepository;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.service.UserService;
import com.example.CineHive.service.board.BoardService;
import com.example.CineHive.service.reply.ReplyBookmarkService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/myPage")
@RequiredArgsConstructor
@Tag(name = "MyPage Controller", description = "마이페이지 관련 기능을 제공하는 API")
public class MyPageController {
    private final JwtTokenUtil jwtTokenUtil;
    private final ReplyBookmarkService replyBookmarkService;
    private final MovieRepository movieRepository;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @GetMapping("/bookmarks")
    @Operation(summary = "찜한 영화 목록 조회", description = "JWT에서 추출한 사용자 email을 기준으로 찜한 영화 정보를 조회")
    public ResponseEntity<?> getFavoriteMovies(HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        System.out.println("받은 토큰: " + token);  // 추가
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            System.out.println("토큰에서 추출한 이메일: " + memEmail);

            List<Long> movieIds = replyBookmarkService.getBookmarkedMovieIdsByEmail(memEmail);

            List<Movie> favoriteMovies = movieRepository.findAllById(movieIds);
            return ResponseEntity.ok(favoriteMovies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }


    @GetMapping("/replies")
    @Operation(summary = "내가 작성한 댓글 조회", description = "JWT에서 추출한 사용자 email을 기준으로 댓글 정보를 조회")
    public ResponseEntity<?> getMyReplies(HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            System.out.println("토큰에서 추출한 이메일: " + memEmail);

            List<Reply> myReplies = replyRepository.findByMemEmail(memEmail);

            return ResponseEntity.ok(myReplies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @GetMapping("/likes")
    @Operation(summary = "내가 좋아요한 댓글 조회", description = "JWT에서 추출한 사용자 email을 기준으로 좋아요한 댓글 정보를 조회")
    public ResponseEntity<?> getMyLikes(HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            System.out.println("토큰에서 추출한 이메일: " + memEmail);

            List<Reply> myLikes = replyRepository.findByMemEmail(memEmail);

            return ResponseEntity.ok(myLikes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @GetMapping("/boards")
    public ResponseEntity<?> getMyBoards(HttpServletRequest request) {
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
            Long memId = user.getMem_id();

            List<Board> boards = boardRepository.findBoardsByMemId(memId);

            // BoardDto 리스트로 변환
            List<BoardDto> boardDtos = boards.stream().map(board -> {
                List<CommentDto> commentDtos = board.getComments().stream()
                        .map(comment -> new CommentDto(
                                comment.getId(),
                                comment.getContent(),
                                comment.getUser().getMemNickname(),
                                comment.getUser().getMemEmail(),
                                comment.getCreatedAt()
                        ))
                        .collect(Collectors.toList());

                return new BoardDto(
                        board.getId(),
                        board.getBrdTitle(),
                        board.getBrdContent(),
                        board.getUser().getMemNickname(),
                        board.getUser().getMemEmail(),
                        board.getBrdRegDate(),
                        board.getBookmarkCount(),
                        board.getLikeCount(),
                        board.getDisLikeCount(),
                        board.getReportCount(),
                        board.getCommentCount(),
                        commentDtos,
                        board.getViews()
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(boardDtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }


    @GetMapping("/comments")
    @Operation(summary = "작성한 댓글 조회", description = "JWT를 통해 인증된 사용자의 댓글 목록을 조회합니다.")
    public ResponseEntity<?> getMyComments(HttpServletRequest request) {
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
            Long memId = user.getMem_id();

            List<Comment> comments = commentRepository.findCommentsByUserId(memId);

            // DTO로 변환
            List<CommentDto> commentDtos = comments.stream()
                    .map(comment -> new CommentDto(
                            comment.getId(),
                            comment.getContent(),
                            comment.getUser().getMemNickname(),
                            comment.getUser().getMemEmail(),
                            comment.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(commentDtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }


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
