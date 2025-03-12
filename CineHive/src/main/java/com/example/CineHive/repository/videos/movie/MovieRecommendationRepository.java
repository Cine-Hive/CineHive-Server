package com.example.CineHive.repository.videos.movie;

import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.RecommendationMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRecommendationRepository extends JpaRepository<RecommendationMovie, Long> {
    List<RecommendationMovie> findByMovie(Movie movie);
}