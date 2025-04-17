package com.example.CineHive.repository.reply;

import com.example.CineHive.entity.reply.ReplyBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReplyBookmarkRepository extends JpaRepository<ReplyBookmark, Long> {

    Optional<ReplyBookmark> findByMemEmailAndMovieId(String memEmail, Long movieId);

    long countByMovieId(Long movieId);

}
