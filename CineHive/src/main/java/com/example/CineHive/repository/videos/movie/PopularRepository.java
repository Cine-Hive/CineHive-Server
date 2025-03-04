package com.example.CineHive.repository.videos.movie;

import com.example.CineHive.entity.videotype.PoPularMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularRepository extends JpaRepository<PoPularMovie, Long> {

}
