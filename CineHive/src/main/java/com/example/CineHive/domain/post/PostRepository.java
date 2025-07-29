package com.example.CineHive.domain.post;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 게시글(Post) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 * N+1 문제 해결을 위해 @EntityGraph를 적극적으로 사용합니다.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 모든 게시글을 조회할 때, N+1 문제를 방지하기 위해 작성자(User) 정보도 함께 조회합니다.
     * @return User 정보가 포함된 Post 엔티티 리스트
     */
    @Override
    @EntityGraph(value = "Post.withUser")
    List<Post> findAll();

    /**
     * ID로 특정 게시글을 조회할 때, N+1 문제를 방지하기 위해 작성자(User) 정보도 함께 조회합니다.
     * @param id 조회할 게시글의 ID
     * @return User 정보가 포함된 Optional<Post> 객체
     */
    @Override
    @EntityGraph(value = "Post.withUser")
    Optional<Post> findById(Long id);

    /**
     * 키워드를 사용하여 게시글을 검색할 때, N+1 문제를 방지하기 위해 작성자(User) 정보도 함께 조회합니다.
     *
     * @param keyword 검색할 키워드 문자열
     * @return User 정보가 포함된 검색 조건에 맞는 게시글 엔티티의 리스트
     */
    @Query("SELECT p FROM Post p JOIN p.user u WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR p.content LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    @EntityGraph(value = "Post.withUser")
    List<Post> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 특정 사용자가 작성한 모든 게시글을 삭제합니다.
     * 주로 회원 탈퇴 시 사용됩니다.
     *
     * @param email 삭제할 게시글의 작성자 이메일
     */
    @Modifying
    @Query("DELETE FROM Post p WHERE p.user.email = :email")
    void deleteAllByUserEmail(@Param("email") String email);
}
