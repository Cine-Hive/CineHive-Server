package com.example.CineHive.repository.post;

import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.Gender;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.domain.user.UserRole;
import com.example.CineHive.domain.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
@DisplayName("PostRepository 성능 벤치마크 테스트")
class PostRepositoryPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(PostRepositoryPerformanceTest.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성: 사용자 10명, 각 사용자당 게시글 10개 (총 100개)
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            users.add(User.builder()
                    .email("user" + i + "@test.com")
                    .password("password")
                    .name("User" + i)
                    .nickname("Nickname" + i)
                    .gender(Gender.OTHER)
                    .role(UserRole.ROLE_USER)
                    .build());
        }
        userRepository.saveAll(users);

        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            posts.add(Post.builder()
                    .title("Test Post " + i)
                    .content("Content for post " + i)
                    .user(users.get(i % 10))
                    .build());
        }
        postRepository.saveAll(posts);

        // 영속성 컨텍스트 초기화
        entityManager.flush();
        entityManager.clear();
        log.info("===== 테스트 데이터 생성 완료 (User 10명, Post 100개) =====");
    }

    @Test
    @DisplayName("N+1 문제가 발생하는 기본 조회 방식 성능 측정")
    void performanceTest_N_Plus_One() {
        log.info("===== [시작] N+1 문제 발생 케이스 =====");
        long startTime = System.currentTimeMillis();

        List<Post> posts = postRepository.findAllNPlusOne();
        // Lazy Loading을 강제로 유발하기 위해 연관 엔티티의 필드에 접근
        posts.forEach(post -> post.getUser().getNickname());

        long endTime = System.currentTimeMillis();
        log.info("===== [종료] N+1 문제 발생 케이스 (실행 시간: {}ms) =====", (endTime - startTime));
        log.info("==> 예상 쿼리 수: 1 (Post 목록) + 10 (각 User) = 11번 (실제로는 더 많을 수 있음)");
    }

    @Test
    @DisplayName("@EntityGraph를 사용한 조회 방식 성능 측정")
    void performanceTest_EntityGraph() {
        log.info("===== [시작] @EntityGraph 해결 케이스 =====");
        long startTime = System.currentTimeMillis();

        List<Post> posts = postRepository.findAll(); // @EntityGraph가 적용된 메서드
        posts.forEach(post -> post.getUser().getNickname());

        long endTime = System.currentTimeMillis();
        log.info("===== [종료] @EntityGraph 해결 케이스 (실행 시간: {}ms) =====", (endTime - startTime));
        log.info("==> 예상 쿼리 수: 1번 (LEFT JOIN)");
    }

    @Test
    @DisplayName("Fetch Join을 사용한 조회 방식 성능 측정")
    void performanceTest_FetchJoin() {
        log.info("===== [시작] Fetch Join 해결 케이스 =====");
        long startTime = System.currentTimeMillis();

        List<Post> posts = postRepository.findAllByFetchJoin();
        posts.forEach(post -> post.getUser().getNickname());

        long endTime = System.currentTimeMillis();
        log.info("===== [종료] Fetch Join 해결 케이스 (실행 시간: {}ms) =====", (endTime - startTime));
        log.info("==> 예상 쿼리 수: 1번 (INNER JOIN)");
    }
}
