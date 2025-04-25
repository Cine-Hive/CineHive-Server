package com.example.CineHive.controller.myPage;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.*;

import com.example.CineHive.entity.reply.Reply;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.*;
import com.example.CineHive.repository.reply.ReplyRepository;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.service.reply.ReplyBookmarkService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final DisLikeRepository dislikeRepository;
    private final BookmarkRepository bookmarkRepository;

    @GetMapping("/info")
    @Operation(summary = "내 정보 조회", description = "JWT를 통해 인증된 사용자의 기본 정보를 조회합니다.")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
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

            // 필요한 정보만 추려서 반환
            return ResponseEntity.ok(
                    new java.util.HashMap<String, Object>() {{
                        put("memName", user.getMemName());
                        put("memEmail", user.getMemEmail());
                        put("memNickname", user.getMemNickname());
                        put("memSex", user.getMemSex());
                        put("genres", user.getGenres());
                    }}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 조회 실패");
        }
    }



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

    @GetMapping("/boardlikes")
    @Operation(summary = "내가 좋아요한 게시글 조회", description = "사용자가 좋아요한 게시글 정보를 조회")
    public ResponseEntity<?> getMyLikes(HttpServletRequest request) {
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

            List<BoardLike> myLikes = likeRepository.findByUser(user);

            List<Map<String, Object>> likedBoards = myLikes.stream()
                    .map(boardLike -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("boardId", boardLike.getBoard().getId());
                        map.put("boardTitle", boardLike.getBoard().getBrdTitle());
                        map.put("boardContent", boardLike.getBoard().getBrdContent());
                        map.put("boardViews", boardLike.getBoard().getViews());
                        map.put("boardRegDate", boardLike.getBoard().getBrdRegDate());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(likedBoards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @GetMapping("/boarddislikes")
    @Operation(summary = "내가 싫어요한 게시글 조회", description = "사용자가 싫어요한 게시글 정보를 조회")
    public ResponseEntity<?> getMydisLikes(HttpServletRequest request) {
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
            List<BoardDisLike> mydislikes  = dislikeRepository.findByUser(user);

            List<Map<String, Object>> dislikedBoards = mydislikes .stream()
                    .map(boardLike -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("boardId", boardLike.getBoard().getId());
                        map.put("boardTitle", boardLike.getBoard().getBrdTitle());
                        map.put("boardContent", boardLike.getBoard().getBrdContent());
                        map.put("boardViews", boardLike.getBoard().getViews());
                        map.put("boardRegDate", boardLike.getBoard().getBrdRegDate());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dislikedBoards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @GetMapping("/boardbookmarks")
    @Operation(summary = "내가 즐겨찾기한 게시글 조회", description = "JWT에서 추출한 사용자 email을 기준으로 즐겨찾기한 게시글 정보를 조회")
    public ResponseEntity<?> getMyBookmarkedBoards(HttpServletRequest request) {
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
            List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);

            List<Map<String, Object>> bookmarkedBoards = bookmarks.stream()
                    .map(bookmark -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("boardId", bookmark.getBoard().getId());
                        map.put("boardTitle", bookmark.getBoard().getBrdTitle());
                        map.put("boardContent", bookmark.getBoard().getBrdContent());
                        map.put("boardViews", bookmark.getBoard().getViews());
                        map.put("boardRegDate", bookmark.getBoard().getBrdRegDate());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(bookmarkedBoards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
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
                                comment.getCreatedAt(),
                                comment.getBoard().getId()   // ✅ boardId 추가
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
                            comment.getUser() != null ? comment.getUser().getMemNickname() : null,
                            comment.getUser() != null ? comment.getUser().getMemEmail() : null,
                            comment.getCreatedAt(),
                            comment.getBoard() != null ? comment.getBoard().getId() : null
                    ))
                    .collect(Collectors.toList());


            return ResponseEntity.ok(commentDtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }
}
