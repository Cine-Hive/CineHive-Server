package com.example.CineHive.repository.board;

import com.example.CineHive.config.JpaAuditingConfig;
import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.user.Gender;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.user.UserRole;
import com.example.CineHive.entity.user.ProviderType;
import com.example.CineHive.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("BoardRepository 테스트")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 정리
        postRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 사용자 생성
        user1 = createAndSaveMember("user1@test.com", "테스터1");
        user2 = createAndSaveMember("user2@test.com", "테스터2");

        // 테스트용 게시글 데이터 생성 및 저장
        postRepository.save(Post.builder()
                .brdTitle("JPA는 정말 신기해요")
                .brdContent("Spring Boot와 JPA를 배우고 있습니다.")
                .member(user1)
                .build());

        postRepository.save(Post.builder()
                .brdTitle("오늘의 점심 메뉴는?")
                .brdContent("김치찌개가 땡기네요. Spring 롤백 기능 최고!")
                .member(user1)
                .build());

        postRepository.save(Post.builder()
                .brdTitle("리액트(React) 질문 있습니다.")
                .brdContent("리액트에서 상태 관리는 어떻게 하나요?")
                .member(user2)
                .build());
    }

    private User createAndSaveMember(String email, String nickname) {
        return userRepository.save(User.builder()
                .email(email)
                .password("password")
                .name(nickname)
                .nickname(nickname)
                .gender(Gender.MALE)
                .provider(ProviderType.LOCAL)
                .role(UserRole.ROLE_USER)
                .build());
    }

    @Test
    @DisplayName("✅ 성공: '제목'으로 검색 시 해당 게시글을 정확히 찾아낸다.")
    void searchByKeyword_title_success() {
        // given
        String keyword = "JPA";

        // when
        List<Post> results = postRepository.searchByKeyword(keyword);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBrdTitle()).contains(keyword);
    }

    @Test
    @DisplayName("✅ 성공: '내용'으로 검색 시 해당 게시글을 정확히 찾아낸다.")
    void searchByKeyword_content_success() {
        // given
        String keyword = "김치찌개";

        // when
        List<Post> results = postRepository.searchByKeyword(keyword);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBrdContent()).contains(keyword);
    }

    @Test
    @DisplayName("✅ 성공: '작성자 닉네임'으로 검색 시 해당 작성자의 모든 게시글을 찾아낸다.")
    void searchByKeyword_authorNickname_success() {
        // given
        String keyword = "테스터1";

        // when
        List<Post> results = postRepository.searchByKeyword(keyword);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(board -> board.getUser().getNickname().equals(keyword));
    }

    @Test
    @DisplayName("✅ 성공: '제목'과 '내용'에 모두 포함된 키워드로 검색 시 중복 없이 결과를 반환한다.")
    void searchByKeyword_titleAndContent_success() {
        // given
        String keyword = "Spring";

        // when
        List<Post> results = postRepository.searchByKeyword(keyword);

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("✅ 성공: '대소문자'를 구분하지 않고 검색 결과를 반환한다.")
    void searchByKeyword_caseInsensitive_success() {
        // given
        String keyword = "react"; // 원문은 'React'

        // when
        List<Post> results = postRepository.searchByKeyword(keyword);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBrdTitle()).contains("React");
    }

    @Test
    @DisplayName("✅ 성공: 검색 결과가 없을 때 빈 리스트를 반환한다.")
    void searchByKeyword_noResult_success() {
        // given
        String keyword = "존재하지 않는 키워드";

        // when
        List<Post> results = postRepository.searchByKeyword(keyword);

        // then
        assertThat(results).isEmpty();
    }
}