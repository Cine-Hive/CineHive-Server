package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.dto.board.BoardSearchDto;
import com.example.CineHive.dto.board.CreateBoardDto;
import com.example.CineHive.dto.board.GetListBoardDto;
import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.mapper.BoardMapper;
import com.example.CineHive.mapper.CommentMapper;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    /*게시글 생성 */
    public Board createBoard(CreateBoardDto createBoardDto) {
        User user = userRepository.findByMemEmail(createBoardDto.getMemEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + createBoardDto.getMemEmail()));

        Board board = new Board();
        board.setBrdTitle(createBoardDto.getBrdTitle());
        board.setBrdContent(createBoardDto.getBrdContent());
        board.setUser(user);

        return boardRepository.save(board);
    }

    /*게시글 상세글 조회 */
    public BoardDto getBoardPostId(Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다."));

        board.increaseViews();
        boardRepository.save(board);

        BoardDto boardDto = BoardMapper.convertToDto(board);

        CommentMapper commentMapper = new CommentMapper();
        List<CommentDto> commentDtos = board.getComments().stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
        boardDto.setComments(commentDtos);

        return boardDto;
    }


    /*게시글 수정 */
    public Board updateBoard(Long id, String brdTitle, String brdContent, String memEmail) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();

            if (!board.getUser().getMemEmail().equals(memEmail)) {
                throw new RuntimeException("사용자가 이 게시글을 수정할 권한이 없습니다.");
            }

            board.setBrdTitle(brdTitle);
            board.setBrdContent(brdContent);
            return boardRepository.save(board);
        } else {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }
    }


    /* 게시글 삭제 */
    public Board deleteBoard(Long id, String memEmail) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다."));

        if (!board.getUser().getMemEmail().equals(memEmail)) {
            throw new RuntimeException("사용자가 이 게시글을 삭제할 권한이 없습니다.");
        }

        boardRepository.delete(board);
        return board;
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
                    dto.setBrdRegDate(board.getBrdRegDate());
                    dto.setLikeCount(board.getLikeCount());
                    dto.setViews(board.getViews());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public List<BoardSearchDto> searchBoards(String keyword) {
        List<Board> boards = boardRepository.searchByKeyword(keyword);
        return boards.stream()
                .map(BoardMapper::convertToSearchDto)
                .collect(Collectors.toList());
    }

    public List<Board> getBoardsByMemEmail(String email) {
        return boardRepository.findByMemEmail(email);
    }
}
