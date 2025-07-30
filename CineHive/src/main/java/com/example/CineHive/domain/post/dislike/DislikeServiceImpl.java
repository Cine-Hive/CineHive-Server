package com.example.CineHive.domain.post.dislike;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.post.like.LikeRepository;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DislikeServiceImpl implements DislikeService {

    private final DislikeRepository dislikeRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void addDislike(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        if (dislikeRepository.existsByUserAndPost(user, post)) {
            throw new BusinessException(ErrorCode.DISLIKE_ALREADY_EXISTS);
        }

        likeRepository.findByUserAndPost(user, post).ifPresent(like -> {
            likeRepository.delete(like);
            post.decreaseLikeCount();
            log.info("사용자 ID: {}가 게시글 ID: {}의 '좋아요'를 취소하고 '싫어요'를 추가했습니다.", user.getId(), post.getId());
        });

        Dislike dislike = Dislike.builder()
                .user(user)
                .post(post)
                .build();
        dislikeRepository.save(dislike);

        post.increaseDislikeCount();
        log.info("사용자 ID: {}가 게시글 ID: {}에 '싫어요'를 눌렀습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeDislike(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        Dislike dislike = dislikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISLIKE_NOT_FOUND));

        dislikeRepository.delete(dislike);

        post.decreaseDislikeCount();
        log.info("사용자 ID: {}가 게시글 ID: {}의 '싫어요'를 취소했습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getDislikeCount(Long postId) {
        Post post = findPostById(postId);
        return post.getDislikeCount();
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