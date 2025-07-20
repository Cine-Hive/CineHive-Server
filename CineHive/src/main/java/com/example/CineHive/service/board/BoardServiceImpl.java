package com.example.CineHive.service.board;

import com.example.CineHive.dto.post.*;
import com.example.CineHive.dto.tmdb.PagedResponse;
import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.user.User;
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
    public PostDetailResponse createBoard(CreatePostRequest request, String memberEmail) {
        User user = findMemberByEmail(memberEmail);

        Post post = Post.builder()
                .brdTitle(request.brdTitle())
                .brdContent(request.brdContent())
                .member(user)
                .build();

        Post savedPost = boardRepository.save(post);
        return BoardMapper.toBoardDto(savedPost);
    }

    @Override
    @Transactional
    public PostDetailResponse getBoardById(Long boardId) {
        Post post = findBoardById(boardId);
        post.increaseViews(); // 조회수 증가는 그대로 유지
        return BoardMapper.toBoardDto(post);
    }

    @Override
    @Transactional
    public PostDetailResponse updateBoard(Long boardId, UpdatePostRequest request, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Post post = findBoardById(boardId);

        // 게시글 소유권 검증 로직을 서비스 레이어에서 명시적으로 처리
        verifyBoardOwnership(post, user);

        post.update(request.brdTitle(), request.brdContent());
        return BoardMapper.toBoardDto(post);
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Post post = findBoardById(boardId);

        // 게시글 소유권 검증 로직을 서비스 레이어에서 명시적으로 처리
        verifyBoardOwnership(post, user);

        boardRepository.delete(post);
    }

    @Override
    public PagedResponse<PostSummaryResponse> getBoards(int page, int size, PostSortType sort) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort.getDbField()));
        Page<Post> boardPage = boardRepository.findAll(pageable);

        return PagedResponse.<PostSummaryResponse>builder()
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
    private User findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * ID를 사용하여 게시글을 찾고, 없으면 BusinessException을 발생시킵니다.
     * @param boardId 찾을 게시글의 ID
     * @return 찾아낸 Board 엔티티
     */
    private Post findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }

    /**
     * 게시글의 소유자와 현재 요청을 보낸 회원이 일치하는지 확인합니다.
     * 일치하지 않으면 BusinessException을 발생시킵니다.
     * @param post 검증할 게시글 엔티티
     * @param user 검증할 회원 엔티티
     */
    private void verifyBoardOwnership(Post post, User user) {
        if (!post.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
