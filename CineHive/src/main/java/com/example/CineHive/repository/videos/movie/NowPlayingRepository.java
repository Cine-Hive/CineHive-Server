package com.example.CineHive.repository.videos.movie;

import com.example.CineHive.entity.videotype.NowPlayingMovie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NowPlayingRepository extends JpaRepository<NowPlayingMovie, Long> {
}
