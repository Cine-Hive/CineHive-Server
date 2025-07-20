package com.example.CineHive.service.account;

import com.example.CineHive.dto.account.AccountInfoResponse;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException; // BusinessException import
import com.example.CineHive.exception.ErrorCode;       // ErrorCode import
import com.example.CineHive.repository.board.PostRepository;
import com.example.CineHive.repository.post.BookmarkRepository;
import com.example.CineHive.repository.post.CommentRepository;
import com.example.CineHive.repository.post.DislikeRepository;
import com.example.CineHive.repository.post.LikeRepository;
import com.example.CineHive.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 탈퇴 시 연관 데이터 삭제를 위한 리포지토리들
    private final BookmarkRepository bookmarkRepository;
    private final DislikeRepository disLikeRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public AccountInfoResponse getAccountInfo(String email) {
        User user = findMemberByEmail(email);
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public void changeNickname(String email, String newNickname) {
        User user = findMemberByEmail(email);
        if (userRepository.existsByNickname(newNickname) && !user.getNickname().equals(newNickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.changeNickname(newNickname);
        log.info("Member {} changed nickname to {}", email, newNickname);
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = findMemberByEmail(email);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.changePassword(passwordEncoder.encode(newPassword));
        log.info("Member {} changed password.", email);
    }

    @Override
    @Transactional
    public void updateGenres(String email, List<String> genres) {
        User user = findMemberByEmail(email);
        user.updateGenres(new HashSet<>(genres));
        log.info("Member {} updated genres.", email);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        log.warn("Deleting all data for member: {}", email);

        bookmarkRepository.deleteByMember_Email(email);
        likeRepository.deleteByMember_Email(email);
        disLikeRepository.deleteByMember_Email(email);
        commentRepository.deleteByMember_Email(email);
        postRepository.deleteByMember_Email(email);

        // 마지막으로 회원 정보 삭제
        userRepository.deleteByEmail(email);
        log.info("Successfully deleted account for member: {}", email);
    }

    /**
     * 이메일로 회원을 조회하는 내부 헬퍼 메서드.
     * @param email 조회할 이메일
     * @return Member 엔티티
     * @throws BusinessException 해당 이메일의 회원이 없을 경우
     */
    private User findMemberByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
                });
    }
}