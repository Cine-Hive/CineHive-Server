package com.example.CineHive.repository.videos.movie;


import com.example.CineHive.entity.videotype.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
