package com.example.CineHive.domain.person.service;

import com.example.CineHive.domain.person.entity.Person;
import com.example.CineHive.domain.person.entity.PersonLike;
import com.example.CineHive.domain.person.repository.PersonLikeRepository;
import com.example.CineHive.domain.person.repository.PersonRepository;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.service.AbstractLikeService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class PersonLikeServiceImpl extends AbstractLikeService<Person, PersonLike> implements PersonLikeService {

    private final PersonService personService;
    private final PersonRepository personRepository;
    private final PersonLikeRepository personLikeRepository;

    public PersonLikeServiceImpl(UserRepository userRepository, PersonService personService, PersonRepository personRepository, PersonLikeRepository personLikeRepository) {
        super(userRepository);
        this.personService = personService;
        this.personRepository = personRepository;
        this.personLikeRepository = personLikeRepository;
    }

    /**
     * [핵심] '좋아요' 대상(Person)을 찾는 로직을 재정의합니다.
     * DB에 없으면 새로 생성하는 'findOrCreate' 로직을 사용합니다.
     */
    @Override
    protected Person findTargetById(Long targetId) {
        return personService.findOrCreatePerson(targetId);
    }

    /**
     * PersonRepository를 반환합니다. (findTargetById를 재정의하여 실제 사용되지는 않음)
     */
    @Override
    protected JpaRepository<Person, Long> getTargetRepository() {
        return this.personRepository;
    }

    /**
     * 특정 사용자가 특정 인물을 이미 '좋아요' 했는지 확인합니다.
     */
    @Override
    protected boolean isAlreadyLiked(User user, Person person) {
        return personLikeRepository.existsByUserAndPerson(user, person);
    }

    /**
     * PersonLike 엔티티를 생성합니다.
     */
    @Override
    protected PersonLike createLikeEntity(User user, Person person) {
        return PersonLike.builder()
                .user(user)
                .person(person)
                .build();
    }

    /**
     * PersonLike 엔티티를 저장합니다.
     */
    @Override
    protected void saveLike(PersonLike personLike) {
        personLikeRepository.save(personLike);
    }

    /**
     * PersonLike 엔티티를 삭제합니다.
     */
    @Override
    protected void deleteLike(User user, Person person) {
        personLikeRepository.deleteByUserAndPerson(user, person);
    }
}