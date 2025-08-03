package com.example.CineHive.domain.post.controller.like;

import com.example.CineHive.domain.common.controller.DomainFinder;
import com.example.CineHive.domain.post.controller.Post;
import com.example.CineHive.domain.post.dislike.DislikeRepository;
import com.example.CineHive.domain.user.controller.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.controller.PostRepository;
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
    private final PostRepository postRepository;
    private final DomainFinder domainFinder;

    @Override
    @Transactional
    public void addLike(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = domainFinder.findPostById(postId);

        if (likeRepository.existsByUserAndPost(user, post)) {
            log.debug("사용자 ID: {}가 이미 게시글 ID: {}에 '좋아요'를 눌렀습니다.", user.getId(), post.getId());
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        int dislikesDeleted = dislikeRepository.deleteByUserAndPost(user, post);
        if (dislikesDeleted > 0) {
            if (postRepository.decreaseDislikeCount(postId) == 0) {
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
            log.info("사용자 ID: {}가 게시글 ID: {}의 '싫어요'를 취소하고 '좋아요'를 추가합니다.", user.getId(), post.getId());
        }

        // '좋아요' 추가
        likeRepository.save(Like.builder().user(user).post(post).build());
        if (postRepository.increaseLikeCount(postId) == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        log.info("사용자 ID: {}가 게시글 ID: {}에 '좋아요'를 추가했습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeLike(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = domainFinder.findPostById(postId);

        int likesDeleted = likeRepository.deleteByUserAndPost(user, post);
        if (likesDeleted == 0) {
            throw new BusinessException(ErrorCode.LIKE_NOT_FOUND);
        }

        if (postRepository.decreaseLikeCount(postId) == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        log.info("사용자 ID: {}가 게시글 ID: {}의 '좋아요'를 취소했습니다.", user.getId(), post.getId());
    }

    @Override
    public int getLikeCount(Long postId) {
        domainFinder.findPostById(postId);
        return likeRepository.countByPost_Id(postId);
    }
}
