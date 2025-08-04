package com.example.CineHive.domain.person.service;

import com.example.CineHive.domain.person.entity.Person;

/**
 * Person 엔티티의 생성 및 조회를 담당하는 서비스 인터페이스입니다.
 */
public interface PersonService {

    /**
     * TMDB ID로 Person 엔티티를 조회하고, 없으면 새로 생성하여 반환합니다.
     * @param personTmdbId '좋아요' 대상이 되는 인물의 TMDB ID
     * @return DB에 저장되거나 조회된 Person 엔티티
     */
    Person findOrCreatePerson(Long personTmdbId);
}