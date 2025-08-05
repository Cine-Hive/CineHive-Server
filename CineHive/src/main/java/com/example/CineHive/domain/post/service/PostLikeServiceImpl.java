package com.example.CineHive.domain.post.service;

import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.domain.post.entity.PostLike;
import com.example.CineHive.domain.post.repository.PostLikeRepository;
import com.example.CineHive.domain.post.repository.PostRepository;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.service.AbstractLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PostLikeServiceImpl extends AbstractLikeService<Post, PostLike> implements com.example.CineHive.domain.post.service.PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public PostLikeServiceImpl(UserRepository userRepository, PostRepository postRepository, PostLikeRepository postLikeRepository) {
        super(userRepository);
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    protected JpaRepository<Post, Long> getTargetRepository() {
        return this.postRepository;
    }

    @Override
    protected boolean isAlreadyLiked(User user, Post post) {
        return postLikeRepository.existsByUserAndPost(user, post);
    }

    @Override
    protected PostLike createLikeEntity(User user, Post post) {
        return PostLike.builder()
                .user(user)
                .post(post)
                .build();
    }

    @Override
    @Transactional
    protected void saveLike(PostLike postLike) {
        postLikeRepository.save(postLike);
        if (postRepository.increaseLikeCount(postLike.getPost().getId()) == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    protected void deleteLike(User user, Post post) {
        PostLike postLike = postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));

        postLikeRepository.delete(postLike);

        if (postRepository.decreaseLikeCount(post.getId()) == 0) {
            log.warn("좋아요 카운트 감소 실패: 게시글을 찾을 수 없습니다. Post ID: {}", post.getId());
        }
    }
}