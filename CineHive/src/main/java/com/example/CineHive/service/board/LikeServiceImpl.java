package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardLike;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.exception.LikeAlreadyExistsException;
import com.example.CineHive.exception.LikeNotFoundException;
import com.example.CineHive.exception.MemberNotFoundException;
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
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        if (likeRepository.existsByMemberAndBoard(member, board)) {
            throw new LikeAlreadyExistsException(member.getId(), board.getId());
        }

        // '싫어요'를 누른 상태였다면 '싫어요'를 취소하고, 게시글의 '싫어요' 카운트를 1 감소시킴
        dislikeRepository.findByMemberAndBoard(member, board).ifPresent(dislike -> {
            dislikeRepository.delete(dislike);
            board.decreaseDislikeCount();
            log.info("Member {}'s dislike for board {} was removed to add a like.", member.getId(), board.getId());
        });

        BoardLike like = BoardLike.builder()
                .member(member)
                .board(board)
                .build();
        likeRepository.save(like);

        board.increaseLikeCount(); // 게시글의 '좋아요' 카운트 1 증가
        log.info("Member {} liked board {}", member.getId(), board.getId());
    }

    @Override
    @Transactional
    public void removeLike(Long boardId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        BoardLike like = likeRepository.findByMemberAndBoard(member, board)
                .orElseThrow(() -> new LikeNotFoundException(member.getId(), board.getId()));

        likeRepository.delete(like);

        board.decreaseLikeCount(); // 게시글의 '좋아요' 카운트 1 감소
        log.info("Member {} removed like from board {}", member.getId(), board.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getLikeCount(Long boardId) {
        // 불필요한 count 쿼리 대신, 엔티티의 필드 값을 직접 반환
        Board board = findBoardById(boardId);
        return board.getLikeCount();
    }

    //== Private Helper Methods ==//

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }
}