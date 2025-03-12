package com.example.CineHive.repository.videos.movie;

import com.example.CineHive.entity.credit.movie.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieActorRepository extends JpaRepository<Actor, Long> {
}
