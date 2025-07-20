package com.example.CineHive.service.board;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.PostDislike;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.board.PostRepository;
import com.example.CineHive.repository.post.DislikeRepository;
import com.example.CineHive.repository.post.LikeRepository;
import com.example.CineHive.repository.user.UserRepository;
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
    public void addLike(Long boardId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Post post = findBoardById(boardId);

        if (likeRepository.existsByMemberAndBoard(user, post)) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        // '싫어요'를 누른 상태였다면 '싫어요'를 취소하고, 게시글의 '싫어요' 카운트를 1 감소시킴
        dislikeRepository.findByMemberAndBoard(user, post).ifPresent(dislike -> {
            dislikeRepository.delete(dislike);
            post.decreaseDislikeCount();
            log.info("Member {}'s dislike for board {} was removed to add a like.", user.getId(), post.getId());
        });

        PostDislike like = PostDislike.builder()
                .member(user)
                .board(post)
                .build();
        likeRepository.save(like);

        post.increaseLikeCount();
        log.info("Member {} liked board {}", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeLike(Long boardId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Post post = findBoardById(boardId);

        PostDislike like = likeRepository.findByMemberAndBoard(user, post)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);

        post.decreaseLikeCount();
        log.info("Member {} removed like from board {}", user.getId(), post.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getLikeCount(Long boardId) {
        Post post = findBoardById(boardId);
        return post.getLikeCount();
    }

    //== Private Helper Methods ==//

    private User findMemberByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Post findBoardById(Long boardId) {
        return postRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }
}
