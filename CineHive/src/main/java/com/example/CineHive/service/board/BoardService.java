package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.*;
import com.example.CineHive.dto.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardService {

    /**
     * 새로운 게시글을 생성합니다.
     * @param request 게시글 생성 요청 DTO
     * @param memberEmail 작성자 회원 이메일
     * @return 생성된 게시글 정보 DTO
     */
    BoardDto createBoard(CreateBoardRequest request, String memberEmail);

    /**
     * 특정 ID의 게시글을 상세 조회합니다. 조회 시 조회수가 1 증가합니다.
     * @param boardId 조회할 게시글 ID
     * @return 조회된 게시글 정보 DTO
     */
    BoardDto getBoardById(Long boardId);

    /**
     * 특정 ID의 게시글을 수정합니다. 작성자 본인만 수정할 수 있습니다.
     * @param boardId 수정할 게시글 ID
     * @param request 게시글 수정 요청 DTO
     * @param memberEmail 수정 요청자 회원 이메일
     * @return 수정된 게시글 정보 DTO
     */
    BoardDto updateBoard(Long boardId, UpdateBoardRequest request, String memberEmail);

    /**
     * 특정 ID의 게시글을 삭제합니다. 작성자 본인만 삭제할 수 있습니다.
     * @param boardId 삭제할 게시글 ID
     * @param memberEmail 삭제 요청자 회원 이메일
     */
    void deleteBoard(Long boardId, String memberEmail);

    /**
     * 게시글 목록을 페이징하여 조회합니다.
     * @return 클라이언트 친화적인 페이징 응답 DTO
     */
    PagedResponse<GetListBoardDto> getBoards(int page, int size, BoardSortType sort);
}