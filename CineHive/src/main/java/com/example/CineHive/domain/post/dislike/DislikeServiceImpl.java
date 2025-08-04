<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/post/dislike/service/DislikeServiceImpl.java
package com.example.CineHive.domain.post.dislike.service;

import com.example.CineHive.domain.common.service.DomainFinder;
import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.domain.post.dislike.entity.Dislike;
import com.example.CineHive.domain.post.dislike.repository.DislikeRepository;
import com.example.CineHive.domain.post.like.repository.LikeRepository;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.repository.PostRepository;
=======
package com.example.CineHive.domain.post.dislike;

import com.example.CineHive.domain.common.DomainFinder;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.post.like.LikeRepository;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.PostRepository;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/post/dislike/DislikeServiceImpl.java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DislikeServiceImpl implements DislikeService {

    private final DislikeRepository dislikeRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final DomainFinder domainFinder;

    @Override
    @Transactional
    public void addDislike(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = domainFinder.findPostById(postId);

        if (dislikeRepository.existsByUserAndPost(user, post)) {
            log.debug("사용자 ID: {}가 이미 게시글 ID: {}에 '싫어요'를 눌렀습니다.", user.getId(), post.getId());
            throw new BusinessException(ErrorCode.DISLIKE_ALREADY_EXISTS);
        }

        // '좋아요'가 있으면 한 번의 쿼리로 삭제하고, 카운트 감소
        int likesDeleted = likeRepository.deleteByUserAndPost(user, post);
        if (likesDeleted > 0) {
            if (postRepository.decreaseLikeCount(postId) == 0) {
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
            log.info("사용자 ID: {}가 게시글 ID: {}의 '좋아요'를 취소하고 '싫어요'를 추가합니다.", user.getId(), post.getId());
        }

        // '싫어요' 추가
        dislikeRepository.save(Dislike.builder().user(user).post(post).build());
        if (postRepository.increaseDislikeCount(postId) == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        log.info("사용자 ID: {}가 게시글 ID: {}에 '싫어요'를 추가했습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeDislike(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = domainFinder.findPostById(postId);

        // 한 번의 쿼리로 '싫어요'를 삭제하고, 삭제된 행이 없으면 예외 발생
        int dislikesDeleted = dislikeRepository.deleteByUserAndPost(user, post);
        if (dislikesDeleted == 0) {
            throw new BusinessException(ErrorCode.DISLIKE_NOT_FOUND);
        }

        if (postRepository.decreaseDislikeCount(postId) == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        log.info("사용자 ID: {}가 게시글 ID: {}의 '싫어요'를 취소했습니다.", user.getId(), post.getId());
    }

    @Override
    public int getDislikeCount(Long postId) {
        domainFinder.findPostById(postId);
        return dislikeRepository.countByPost_Id(postId);
    }
}
