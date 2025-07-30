package com.example.CineHive.domain.post.like;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.dislike.DislikeRepository;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

        dislikeRepository.findByUserAndPost(user, post).ifPresent(dislike -> {
            dislikeRepository.delete(dislike);
            postRepository.decreaseDislikeCount(postId);
            log.info("사용자 ID: {}가 게시글 ID: {}의 '싫어요'를 취소하고 '좋아요'를 추가합니다.", user.getId(), post.getId());
        });

        Like like = Like.builder()
                .user(user)
                .post(post)
                .build();
        likeRepository.save(like);
        postRepository.increaseLikeCount(postId);
        log.info("사용자 ID: {}가 게시글 ID: {}에 '좋아요'를 눌렀습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeLike(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);
        postRepository.decreaseLikeCount(postId);
        log.info("사용자 ID: {}가 게시글 ID: {}의 '좋아요'를 취소했습니다.", user.getId(), post.getId());
    }

    @Override
    public int getLikeCount(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        return likeRepository.countByPost_Id(postId);
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
