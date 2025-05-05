package com.example.CineHive.repository.reply;

import com.example.CineHive.entity.reply.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByMovieId(Long movieId);
    List<Reply> findByMemEmail(String memEmail);

    List<Reply> findByMemNickname(String memNickname);

    void deleteByMemEmail(String memEmail);
}
