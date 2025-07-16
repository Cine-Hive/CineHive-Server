package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardDislike;
import com.example.CineHive.entity.member.Member;
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
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        if (dislikeRepository.existsByMemberAndBoard(member, board)) {
            throw new BusinessException(ErrorCode.DISLIKE_ALREADY_EXISTS);
        }

        // '좋아요'를 누른 상태였다면 '좋아요'를 취소하고, 게시글의 '좋아요' 카운트를 1 감소시킴
        likeRepository.findByMemberAndBoard(member, board).ifPresent(like -> {
            likeRepository.delete(like);
            board.decreaseLikeCount();
            log.info("Member {}'s like for board {} was removed to add a dislike.", member.getId(), board.getId());
        });

        BoardDislike dislike = BoardDislike.builder()
                .member(member)
                .board(board)
                .build();
        dislikeRepository.save(dislike);

        board.increaseDislikeCount();
        log.info("Member {} disliked board {}", member.getId(), board.getId());
    }

    @Override
    @Transactional
    public void removeDislike(Long boardId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        BoardDislike dislike = dislikeRepository.findByMemberAndBoard(member, board)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISLIKE_NOT_FOUND));

        dislikeRepository.delete(dislike);

        board.decreaseDislikeCount();
        log.info("Member {} removed dislike from board {}", member.getId(), board.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getDislikeCount(Long boardId) {
        Board board = findBoardById(boardId);
        return board.getDislikeCount();
    }

    //== Private Helper Methods ==//

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }
}
