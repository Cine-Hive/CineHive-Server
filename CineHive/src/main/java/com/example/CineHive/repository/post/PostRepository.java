package com.example.CineHive.repository.post;

import com.example.CineHive.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시글(Post) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 키워드를 사용하여 게시글을 검색합니다.
     * 제목과 작성자 닉네임은 대소문자를 무시하고, 내용은 대소문자를 구분하여 검색합니다.
     *
     * @param keyword 검색할 키워드 문자열
     * @return 검색 조건에 맞는 게시글 엔티티의 리스트
     */
    @Query("SELECT p FROM Post p JOIN p.user u WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR p.content LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
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