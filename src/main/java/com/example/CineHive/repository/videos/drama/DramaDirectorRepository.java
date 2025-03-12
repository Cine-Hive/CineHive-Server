package com.example.CineHive.repository.videos.drama;



import com.example.CineHive.entity.videotype.Drama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DramaDirectorRepository extends JpaRepository<Drama, Long> {
}
