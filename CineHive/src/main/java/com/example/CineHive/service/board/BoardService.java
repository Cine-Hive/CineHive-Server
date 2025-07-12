package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.CreateBoardRequest;
import com.example.CineHive.dto.board.GetListBoardDto;
import com.example.CineHive.dto.board.UpdateBoardRequest;

import java.util.List;

/**
 * 게시글(Board) 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 컨트롤러는 이 인터페이스에 의존하여 실제 구현과 분리됩니다.
 */
public interface BoardService {

    /**
     * 새로운 게시글을 생성합니다.
     *
     * @param request     게시글 생성에 필요한 데이터(제목, 내용)를 담은 DTO
     * @param memberEmail 게시글을 작성하는 회원의 이메일 (인증된 사용자 정보)
     * @return 생성된 게시글의 상세 정보를 담은 DTO
     */
    BoardDto createBoard(CreateBoardRequest request, String memberEmail);

    /**
     * ID를 기준으로 특정 게시글의 상세 정보를 조회합니다.
     * 이 메서드 호출 시 해당 게시글의 조회수가 1 증가합니다.
     *
     * @param boardId 조회할 게시글의 고유 ID
     * @return 게시글의 상세 정보를 담은 DTO
     */
    BoardDto getBoardById(Long boardId);

    /**
     * 기존 게시글의 내용을 수정합니다.
     * 내부적으로 수정 권한이 있는지 확인하는 로직이 포함됩니다.
     *
     * @param boardId     수정할 게시글의 고유 ID
     * @param request     수정할 게시글의 데이터(제목, 내용)를 담은 DTO
     * @param memberEmail 수정을 요청하는 회원의 이메일 (인증된 사용자 정보)
     * @return 수정된 게시글의 상세 정보를 담은 DTO
     */
    BoardDto updateBoard(Long boardId, UpdateBoardRequest request, String memberEmail);

    /**
     * 특정 게시글을 삭제합니다.
     * 내부적으로 삭제 권한이 있는지 확인하는 로직이 포함됩니다.
     *
     * @param boardId     삭제할 게시글의 고유 ID
     * @param memberEmail 삭제를 요청하는 회원의 이메일 (인증된 사용자 정보)
     */
    void deleteBoard(Long boardId, String memberEmail);

    /**
     * 모든 게시글의 목록을 조회합니다.
     * 게시글 목록 화면에 표시될 요약된 정보를 반환합니다.
     *
     * @return {@code GetListBoardDto} 객체들의 리스트
     */
    List<GetListBoardDto> getAllBoards();

    /**
     * 키워드를 사용하여 게시글을 검색합니다. (필요 시 주석 해제 후 구현)
     *
     * @param keyword 검색할 키워드
     * @return 검색 결과에 해당하는 {@code BoardSearchDto} 객체들의 리스트
     */
    // List<BoardSearchDto> searchBoards(String keyword);
}