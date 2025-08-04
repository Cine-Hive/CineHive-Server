package com.example.CineHive.domain.person.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.domain.person.entity.Person;
import com.example.CineHive.domain.person.repository.PersonRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final TmdbApiClient tmdbApiClient;

    @Override
    @Transactional
    public Person findOrCreatePerson(Long personTmdbId) {
        return personRepository.findByTmdbId(personTmdbId)
                .orElseGet(() -> {
                    try {
                        log.debug("DB에 인물 정보가 없어 TMDB API를 통해 새로 생성합니다. tmdbId: {}", personTmdbId);

                        // TODO: TmdbApiClient에 인물 상세 정보를 가져오는 getPersonDetail(personTmdbId) 메서드 구현 필요
                        // var tmdbPerson = tmdbApiClient.getPersonDetail(personTmdbId);
                        // Person newPerson = Person.from(tmdbPerson); // Person 엔티티의 정적 팩토리 메서드 사용

                        // 임시 구현
                        Person newPerson = Person.builder()
                                .tmdbId(personTmdbId)
                                .name("인물 " + personTmdbId)
                                .profilePath(null)
                                .build();

                        return personRepository.save(newPerson);
                    } catch (DataIntegrityViolationException e) {
                        log.warn("Race condition 발생: 인물 동시 생성 시도. tmdbId: {}. 재조회합니다.", personTmdbId);
                        return personRepository.findByTmdbId(personTmdbId)
                                .orElseThrow(() -> new BusinessException("인물 정보를 다시 조회하는 데 실패했습니다.", ErrorCode.INTERNAL_SERVER_ERROR));
                    }
                });
    }
}