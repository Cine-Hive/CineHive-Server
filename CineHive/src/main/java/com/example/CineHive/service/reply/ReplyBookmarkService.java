package com.example.CineHive.service.reply;

import com.example.CineHive.entity.reply.ReplyBookmark;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.repository.reply.ReplyBookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyBookmarkService {

    @Autowired
    private ReplyBookmarkRepository replyBookmarkRepository;

    //사용자의 이메일과 영화 id를 통해 즐겨찾기 추가
    @Transactional
    public boolean toggleBookmark(String memEmail, Long movieId) {
        Optional<ReplyBookmark> existingBookmark = replyBookmarkRepository.findByMemEmailAndMovieId(memEmail, movieId);
        if (existingBookmark.isPresent()) {
            // 이미 존재하면 삭제
            replyBookmarkRepository.delete(existingBookmark.get());
            return false; // 삭제됨 (false 반환)
        } else {
            // 존재하지 않으면 추가
            ReplyBookmark bookmark = new ReplyBookmark(null, memEmail, movieId);
            replyBookmarkRepository.save(bookmark);
            return true; // 추가됨 (true 반환)
        }
    }

    public long getBookmarkCount(Long movieId) {
        return replyBookmarkRepository.countByMovieId(movieId);
    }

    public List<Long> getBookmarkedMovieIdsByEmail(String memEmail) {
        return replyBookmarkRepository.findMovieIdsByMemEmail(memEmail);
    }


}
