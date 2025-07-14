package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.*;
import com.example.CineHive.dto.response.PagedResponse;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.exception.MemberNotFoundException;
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
        board.increaseViews();
        return BoardMapper.toBoardDto(board);
    }

    @Override
    @Transactional
    public BoardDto updateBoard(Long boardId, UpdateBoardRequest request, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        board.verifyOwnership(member);
        board.update(request.brdTitle(), request.brdContent());

        return BoardMapper.toBoardDto(board);
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Board board = findBoardById(boardId);

        board.verifyOwnership(member);
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

    /**
     * 이메일을 사용하여 회원을 찾고, 없으면 MemberNotFoundException을 발생시킵니다.
     * @param email 찾을 회원의 이메일
     * @return 찾아낸 Member 엔티티
     * @throws MemberNotFoundException 해당 이메일의 회원이 존재하지 않을 경우
     */
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    /**
     * ID를 사용하여 게시글을 찾고, 없으면 BoardNotFoundException을 발생시킵니다.
     * @param boardId 찾을 게시글의 ID
     * @return 찾아낸 Board 엔티티
     * @throws BoardNotFoundException 해당 ID의 게시글이 존재하지 않을 경우
     */
    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }

    /**
     * 정렬 기준 문자열을 기반으로 Sort 객체를 생성하는 private 헬퍼 메서드.
     */
    private Sort createSort(String sort) {
        return switch (sort.toLowerCase()) {
            case "views" -> Sort.by("views").descending();
            case "likes" -> Sort.by("likeCount").descending();
            // "latest" 및 기타 모든 알 수 없는 값에 대해 기본적으로 최신순 정렬
            default -> Sort.by("createdAt").descending();
        };
    }
}