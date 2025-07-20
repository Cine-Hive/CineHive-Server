package com.example.CineHive.service.account;

import com.example.CineHive.dto.account.AccountInfoResponse;
import com.example.CineHive.dto.account.UpdateGenresRequest;
import com.example.CineHive.dto.account.UpdateNicknameRequest;
import com.example.CineHive.dto.account.UpdatePasswordRequest;
import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.post.*; // post 패키지로 변경
import com.example.CineHive.repository.user.UserRepository;
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

    // 회원 탈퇴 시 연관 데이터 삭제를 위한 리포지토리들
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
        log.info("User {} changed nickname to {}", email, newNickname);
    }

    @Override
    @Transactional
    public void changePassword(String email, UpdatePasswordRequest request) {
        User user = findUserByEmail(email);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        log.info("User {} changed password.", email);
    }

    @Override
    @Transactional
    public void updateGenres(String email, UpdateGenresRequest request) {
        User user = findUserByEmail(email);
        Set<Genre> newGenres = request.genres().stream()
                .map(genreName -> Genre.valueOf(genreName.toUpperCase()))
                .collect(Collectors.toSet());
        user.updateGenres(newGenres);
        log.info("User {} updated genres.", email);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        log.warn("Deleting all data for user: {}", email);

        // 리팩토링된 리포지토리 메서드 호출
        bookmarkRepository.deleteAllByUserEmail(email);

        likeRepository.deleteAllByUserEmail(email);
        dislikeRepository.deleteAllByUserEmail(email);
        commentRepository.deleteAllByUser_Email(email);
        postRepository.deleteAllByUserEmail(email);

        userRepository.deleteByEmail(email);
        log.info("Successfully deleted account for user: {}", email);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}