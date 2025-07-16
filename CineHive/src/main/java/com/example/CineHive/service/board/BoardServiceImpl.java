package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.*;
import com.example.CineHive.dto.response.PagedResponse;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.mapper.BoardMapper;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public BoardDto createBoard(CreateBoardRequest request, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);

        Board board = Board.builder()
                .brdTitle(request.brdTitle())
                .brdContent(request.brdContent())
                .member(member)
                .build();

        Board savedBoard = boardRepository.save(board);
        return BoardMapper.toBoardDto(savedBoard);
    }

    @Override
    @Transactional
    public BoardDto getBoardById(Long boardId) {
        Board board = findBoardById(boardId);
        board.increaseViews(); // 조회수 증가는 그대로 유지
        return BoardMapper.toBoardDto(board);
    }

    @Override
    @Transactional
    public BoardDto updateBoard(Long boardId, UpdateBoardRequest request, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        // 게시글 소유권 검증 로직을 서비스 레이어에서 명시적으로 처리
        verifyBoardOwnership(board, member);

        board.update(request.brdTitle(), request.brdContent());
        return BoardMapper.toBoardDto(board);
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        // 게시글 소유권 검증 로직을 서비스 레이어에서 명시적으로 처리
        verifyBoardOwnership(board, member);

        boardRepository.delete(board);
    }

    @Override
    public PagedResponse<GetListBoardDto> getBoards(int page, int size, BoardSortType sort) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort.getDbField()));
        Page<Board> boardPage = boardRepository.findAll(pageable);

        return PagedResponse.<GetListBoardDto>builder()
                .content(boardPage.getContent().stream().map(BoardMapper::toListDto).toList())
                .page(boardPage.getNumber() + 1)
                .size(boardPage.getSize())
                .totalElements(boardPage.getTotalElements())
                .totalPages(boardPage.getTotalPages())
                .last(boardPage.isLast())
                .build();
    }

    //== private 헬퍼 메서드 ==//

    /**
     * 이메일을 사용하여 회원을 찾고, 없으면 BusinessException을 발생시킵니다.
     * @param email 찾을 회원의 이메일
     * @return 찾아낸 Member 엔티티
     */
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * ID를 사용하여 게시글을 찾고, 없으면 BusinessException을 발생시킵니다.
     * @param boardId 찾을 게시글의 ID
     * @return 찾아낸 Board 엔티티
     */
    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }

    /**
     * 게시글의 소유자와 현재 요청을 보낸 회원이 일치하는지 확인합니다.
     * 일치하지 않으면 BusinessException을 발생시킵니다.
     * @param board 검증할 게시글 엔티티
     * @param member 검증할 회원 엔티티
     */
    private void verifyBoardOwnership(Board board, Member member) {
        if (!board.getMember().equals(member)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
