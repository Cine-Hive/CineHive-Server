package com.example.CineHive.domain.common;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.domain.user.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 여러 도메인에서 공통적으로 사용되는 엔티티 조회 로직을 중앙에서 관리하는 헬퍼 클래스입니다.
 * 중복 코드를 제거하고 일관된 예외 처리를 보장합니다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainFinder {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
