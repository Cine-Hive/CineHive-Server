package com.example.CineHive.domain.person.repository;

import com.example.CineHive.domain.person.entity.Person;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Person 엔티티에 대한 데이터 접근을 처리하는 Repository입니다.
 */
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * TMDB ID를 기준으로 Person 엔티티를 조회합니다.
     * tmdbId 컬럼에 인덱스가 설정되어 있어 빠른 조회가 가능합니다.
     *
     * @param tmdbId 조회할 인물의 TMDB ID
     * @return Optional<Person> 조회된 Person 엔티티. 없을 경우 Optional.empty()
     */
    Optional<Person> findByTmdbId(Long tmdbId);

    /**
     * TMDB ID로 Person을 조회하되, 없을 경우 EntityNotFoundException을 던지는 편의 메서드입니다.
     * @param tmdbId 조회할 인물의 TMDB ID
     * @return 조회된 Person 엔티티
     * @throws EntityNotFoundException 해당 tmdbId의 Person이 존재하지 않을 경우
     */
    default Person getByTmdbId(Long tmdbId) {
        return findByTmdbId(tmdbId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with tmdbId: " + tmdbId));
    }
}