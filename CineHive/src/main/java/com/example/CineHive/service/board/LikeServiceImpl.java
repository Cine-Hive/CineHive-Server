package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardLike;
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
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final DislikeRepository dislikeRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public void addLike(Long boardId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        if (likeRepository.existsByMemberAndBoard(user, board)) {
            throw new BusinessException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        // '싫어요'를 누른 상태였다면 '싫어요'를 취소하고, 게시글의 '싫어요' 카운트를 1 감소시킴
        dislikeRepository.findByMemberAndBoard(user, board).ifPresent(dislike -> {
            dislikeRepository.delete(dislike);
            board.decreaseDislikeCount();
            log.info("Member {}'s dislike for board {} was removed to add a like.", user.getId(), board.getId());
        });

        BoardLike like = BoardLike.builder()
                .member(user)
                .board(board)
                .build();
        likeRepository.save(like);

        board.increaseLikeCount();
        log.info("Member {} liked board {}", user.getId(), board.getId());
    }

    @Override
    @Transactional
    public void removeLike(Long boardId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        BoardLike like = likeRepository.findByMemberAndBoard(user, board)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));

        likeRepository.delete(like);

        board.decreaseLikeCount();
        log.info("Member {} removed like from board {}", user.getId(), board.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getLikeCount(Long boardId) {
        Board board = findBoardById(boardId);
        return board.getLikeCount();
    }

    //== Private Helper Methods ==//

    private User findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }
}
