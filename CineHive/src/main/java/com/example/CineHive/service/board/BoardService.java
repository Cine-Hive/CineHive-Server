package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.GetListBoardDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.mapper.BoardMapper;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class  BoardService {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    /*게시글 생성 */
    public Board createBoard(BoardDto boardDto) {
        User user = userRepository.findByMemEmail(boardDto.getMemEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + boardDto.getMemEmail()  ));

        Board board = new Board();
        board.setBrdTitle(boardDto.getBrdTitle());
        board.setBrdContent(boardDto.getBrdContent());
        board.setUser(user);

        return boardRepository.save(board);
    }

    /*게시글 상세글 조회 */
    public BoardDto getBoardPostId(Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다."));

        board.increaseViews();
        boardRepository.save(board);

        return BoardMapper.convertToDto(board);
    }

    /*게시글 수정 */
    public Board updateBoard(Long id, String brdTitle, String brdContent) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            board.setBrdTitle(brdTitle);
            board.setBrdContent(brdContent);
            return boardRepository.save(board);
        } else {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }
    }

    /*게시글 삭제 */
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다."));
        boardRepository.delete(board);
    }

    /*게시글 전체 목록 조회 */
    public List<GetListBoardDto> getAllBoard() {
        List<Board> boards = boardRepository.findAll();
        return boards.stream()
                .map(board -> {
                    GetListBoardDto dto = new GetListBoardDto();
                    dto.setId(board.getId());
                    dto.setBrdTitle(board.getBrdTitle());
                    dto.setBrdContent(board.getBrdContent());
                    dto.setMemNickname(board.getUser().getMemNickname());
                    dto.setBrgRegDate(board.getBrdRegDate());
                    dto.setLikeCount(board.getLikeCount());
                    dto.setViews(board.getViews());
                    return dto;
                })
                .collect(Collectors.toList());
    }



    public List<BoardDto> searchBoards(String keyword) {
        List<Board> boards = boardRepository.searchByKeyword(keyword);
        return boards.stream()
                .map(BoardMapper::convertToDto)
                .collect(Collectors.toList());
    }
}
