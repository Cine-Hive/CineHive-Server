package com.example.CineHive.repository.reply;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.CineHive.entity.reply.ReplyBookmark;
import com.example.CineHive.entity.videotype.Movie;

public interface ReplyBookmarkRepository extends JpaRepository<ReplyBookmark, Long> {

    Optional<ReplyBookmark> findByMemEmailAndMovieId(String memEmail, Long movieId);

    long countByMovieId(Long movieId);

    @Query("SELECT r.movieId FROM ReplyBookmark r WHERE r.memEmail = :memEmail")
    List<Long> findMovieIdsByMemEmail(@Param("memEmail") String memEmail);



}
