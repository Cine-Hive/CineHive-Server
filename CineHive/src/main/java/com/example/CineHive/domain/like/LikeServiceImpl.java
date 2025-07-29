package com.example.CineHive.domain.like;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.dislike.DislikeRepository;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final DislikeRepository dislikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void addLike(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        if (likeRepository.existsByUserAndPost(user, post)) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        // '싫어요'를 누른 상태였다면 '싫어요'를 취소
        dislikeRepository.findByUserAndPost(user, post).ifPresent(dislike -> {
            dislikeRepository.delete(dislike);
            post.decreaseDislikeCount();
            log.info("사용자 ID: {}가 게시글 ID: {}의 '싫어요'를 취소하고 '좋아요'를 추가했습니다.", user.getId(), post.getId());
        });

        // PostLike 엔티티를 생성하고 저장 (버그 수정)
        PostLike like = PostLike.builder()
                .user(user)
                .post(post)
                .build();
        likeRepository.save(like);

        post.increaseLikeCount();
        log.info("사용자 ID: {}가 게시글 ID: {}에 '좋아요'를 눌렀습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeLike(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        // PostLike 엔티티를 조회 (버그 수정)
        PostLike like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);

        post.decreaseLikeCount();
        log.info("사용자 ID: {}가 게시글 ID: {}의 '좋아요'를 취소했습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getLikeCount(Long postId) {
        Post post = findPostById(postId);
        return post.getLikeCount();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}