package com.example.CineHive.service.board;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.PostLike;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.DislikeRepository;
import com.example.CineHive.repository.board.LikeRepository;
import com.example.CineHive.repository.member.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public void addDislike(Long boardId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Post post = findBoardById(boardId);

        if (dislikeRepository.existsByMemberAndBoard(user, post)) {
            throw new BusinessException(ErrorCode.DISLIKE_ALREADY_EXISTS);
        }

        // '좋아요'를 누른 상태였다면 '좋아요'를 취소하고, 게시글의 '좋아요' 카운트를 1 감소시킴
        likeRepository.findByMemberAndBoard(user, post).ifPresent(like -> {
            likeRepository.delete(like);
            post.decreaseLikeCount();
            log.info("Member {}'s like for board {} was removed to add a dislike.", user.getId(), post.getId());
        });

        PostLike dislike = PostLike.builder()
                .member(user)
                .board(post)
                .build();
        dislikeRepository.save(dislike);

        post.increaseDislikeCount();
        log.info("Member {} disliked board {}", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeDislike(Long boardId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Post post = findBoardById(boardId);

        PostLike dislike = dislikeRepository.findByMemberAndBoard(user, post)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISLIKE_NOT_FOUND));

        dislikeRepository.delete(dislike);

        post.decreaseDislikeCount();
        log.info("Member {} removed dislike from board {}", user.getId(), post.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getDislikeCount(Long boardId) {
        Post post = findBoardById(boardId);
        return post.getDislikeCount();
    }

    //== Private Helper Methods ==//

    private User findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Post findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }
}
