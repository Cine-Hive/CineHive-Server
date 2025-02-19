package com.example.CineHive.repository.reply;

import com.example.CineHive.entity.User;
import com.example.CineHive.entity.reply.Review;
import com.example.CineHive.entity.videotype.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieId(Long movieId);
    List<Review> findByUserEmail(String userEmail);

}
