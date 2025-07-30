package com.example.CineHive.domain.user;

import com.example.CineHive.domain.post.bookmark.BookmarkRepository;
import com.example.CineHive.domain.post.comment.CommentRepository;
import com.example.CineHive.domain.post.dislike.DislikeRepository;
import com.example.CineHive.domain.post.like.LikeRepository;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.dto.AccountInfoResponse;
import com.example.CineHive.domain.user.dto.UpdateGenresRequest;
import com.example.CineHive.domain.user.dto.UpdateNicknameRequest;
import com.example.CineHive.domain.user.dto.UpdatePasswordRequest;
import com.example.CineHive.domain.media.Genre;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final BookmarkRepository bookmarkRepository;
    private final DislikeRepository dislikeRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public AccountInfoResponse getAccountInfo(String email) {
        User user = findUserByEmail(email);
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public void changeNickname(String email, UpdateNicknameRequest request) {
        User user = findUserByEmail(email);
        String newNickname = request.nickname();
        if (userRepository.existsByNickname(newNickname) && !user.getNickname().equals(newNickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.changeNickname(newNickname);
        log.info("사용자({}), 닉네임을 '{}'(으)로 변경했습니다.", email, newNickname);
    }

    @Override
    @Transactional
    public void changePassword(String email, UpdatePasswordRequest request) {
        User user = findUserByEmail(email);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        log.info("사용자({}), 비밀번호를 변경했습니다.", email);
    }

    @Override
    @Transactional
    public void updateGenres(String email, UpdateGenresRequest request) {
        User user = findUserByEmail(email);
        Set<Genre> newGenres = request.genres().stream()
                .map(genreName -> Genre.valueOf(genreName.toUpperCase()))
                .collect(Collectors.toSet());
        user.updateGenres(newGenres);
        log.info("사용자({}), 선호 장르를 업데이트했습니다.", email);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        log.warn("사용자({})의 모든 데이터를 삭제합니다. (회원 탈퇴)", email);

        bookmarkRepository.deleteAllByUserEmail(email);
        likeRepository.deleteAllByUserEmail(email);
        dislikeRepository.deleteAllByUserEmail(email);
        commentRepository.deleteAllByUserEmail(email);
        postRepository.deleteAllByUserEmail(email);
        userRepository.deleteByEmail(email);

        log.info("사용자({}) 계정이 성공적으로 삭제되었습니다.", email);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}