package com.example.CineHive.global.config.init;

import com.example.CineHive.domain.banner.Banner;
import com.example.CineHive.domain.banner.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BannerDataInitializer implements ApplicationRunner {

    private final BannerRepository bannerRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (bannerRepository.count() > 0) {
            log.info("기존 배너 데이터가 존재하므로, 초기화를 건너뜁니다.");
            return;
        }

        log.info("기본 배너 데이터를 생성합니다...");

        List<Banner> banners = List.of(
                // --- 활성화된 배너 (5개) ---
                Banner.builder()
                        .title("이번 주, 전 세계가 열광하는 영화는?")
                        .subtitle("주간 트렌드 차트에서 지금 바로 확인하세요")
                        .imageUrl("/jXJxMcVoEuXzym3pFgnq1thbdsE.jpg") // 듄
                        .linkUrl("/api/v1/media/charts/TRENDING_MOVIES_WEEK")
                        .displayOrder(1)
                        .isActive(true)
                        .build(),
                Banner.builder()
                        .title("명작은 시간을 초월합니다")
                        .subtitle("역대 최고 평점 TV 시리즈 컬렉션")
                        .imageUrl("/9zcbqSxdsRMZ2AbDBVriC1Wlg7a.jpg") // 브레이킹 배드
                        .linkUrl("/api/v1/media/charts/TOP_RATED_TV")
                        .displayOrder(2)
                        .isActive(true)
                        .build(),
                Banner.builder()
                        .title("마블 시네마틱 유니버스 정주행")
                        .subtitle("페이즈 1부터 최신작까지 한눈에")
                        .imageUrl("/9BBTo63ANSmhC4eMCb6rBJl7KCN.jpg") // 어벤져스
                        .linkUrl("/api/v1/media/charts/MARVEL_UNIVERSE_MOVIES")
                        .displayOrder(3)
                        .isActive(true)
                        .build(),
                Banner.builder()
                        .title("K-드라마의 저력")
                        .subtitle("전 세계를 사로잡은 한국 드라마 시리즈")
                        .imageUrl("/dKqa8rNSSw6kC8BwC1t2qD9gW6B.jpg") // 오징어 게임
                        .linkUrl("/api/v1/media/charts/KOREAN_DRAMA_SERIES")
                        .displayOrder(4)
                        .isActive(true)
                        .build(),
                Banner.builder()
                        .title("픽사의 마법 같은 세상으로")
                        .subtitle("어른과 아이 모두를 위한 감동 애니메이션")
                        .imageUrl("/j29ekbcLpBvxnGk6LjdTc2EI5SA.jpg") // 인사이드 아웃
                        .linkUrl("/api/v1/media/charts/PIXAR_ANIMATION_COLLECTION")
                        .displayOrder(5)
                        .isActive(true)
                        .build(),

                // --- 비활성화된 배너 (15개) ---
                Banner.builder()
                        .title("심장이 쫄깃해지는 스릴러")
                        .subtitle("잠 못 드는 밤을 위한 추천작")
                        .imageUrl("/TU9NIjwzjoKPwQHoHshkFcQUCG.jpg") // 기생충
                        .linkUrl("/api/v1/media/charts/THRILLER_MUST_WATCH")
                        .displayOrder(6)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("웃음이 필요할 때")
                        .subtitle("오늘의 코미디 인기작")
                        .imageUrl("/8Y43POKjjKDGI9mh893Z6xIYksT.jpg") // 바비
                        .linkUrl("/api/v1/media/charts/COMEDY_FAVORITES")
                        .displayOrder(7)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("지브리 스튜디오 특별전")
                        .subtitle("미야자키 하야오의 환상적인 세계")
                        .imageUrl("/Ab8K2gdr8u8jV6gVd21sJ7n0n1u.jpg") // 센과 치히로의 행방불명
                        .linkUrl("/api/v1/media/charts/STUDIO_GHIBLI_MOVIES")
                        .displayOrder(8)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("A24, 믿고 보는 이름")
                        .subtitle("독창적이고 감각적인 A24 스튜디오 작품선")
                        .imageUrl("/7B71k7dCgV8N4a4L7IO3B0pn13e.jpg") // 에브리씽 에브리웨어 올 앳 원스
                        .linkUrl("/api/v1/media/charts/A24_FILMS_SELECTION")
                        .displayOrder(9)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("넷플릭스 오리지널 시리즈")
                        .subtitle("오직 넷플릭스에서만")
                        .imageUrl("/56v2KjBlU4XaOv9rVYEQypROD7P.jpg") // 기묘한 이야기
                        .linkUrl("/api/v1/media/charts/platforms/NETFLIX")
                        .displayOrder(10)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("디즈니+ 오리지널 시리즈")
                        .subtitle("상상력이 현실이 되는 곳")
                        .imageUrl("/uNyEVSPeAtJgUBeijk0gUap4u1V.jpg") // 만달로리안
                        .linkUrl("/api/v1/media/charts/platforms/DISNEY_PLUS")
                        .displayOrder(11)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("시간 여행 떠나볼까요?")
                        .subtitle("과거와 미래를 넘나드는 모험")
                        .imageUrl("/fBq2Jd2v0gmdxP2d2K267OrP3iB.jpg") // 백 투 더 퓨쳐
                        .linkUrl("/api/v1/media/charts/TIME_TRAVEL_ADVENTURES")
                        .displayOrder(12)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("좀비 아포칼립스에서 살아남기")
                        .subtitle("극한의 생존 스릴")
                        .imageUrl("/xK2sC0nke3pA3aIs2nSGC4d52aZ.jpg") // 부산행
                        .linkUrl("/api/v1/media/charts/ZOMBIE_APOCALYPSE_SURVIVAL")
                        .displayOrder(13)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("HBO 명작 시리즈")
                        .subtitle("차원이 다른 드라마의 깊이")
                        .imageUrl("/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg") // 왕좌의 게임
                        .linkUrl("/api/v1/media/charts/platforms/HBO")
                        .displayOrder(14)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("2000년대 하이틴 영화")
                        .subtitle("Y2K 감성 가득, 그 시절 우리가 사랑한 영화들")
                        .imageUrl("/AbQ53gP26aA4g6i3i2T2lI4Hk24.jpg") // 퀸카로 살아남는 법
                        .linkUrl("/api/v1/media/charts/HIGH_SCHOOL_TEEN_MOVIES")
                        .displayOrder(15)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("DC 유니버스 히어로즈")
                        .subtitle("어둠의 기사부터 최강의 영웅까지")
                        .imageUrl("/nNmJRkg8wWnRmzQDe2FwKbPIsJV.jpg") // 다크 나이트
                        .linkUrl("/api/v1/media/charts/DC_UNIVERSE_MOVIES")
                        .displayOrder(16)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("일본 애니메이션 시리즈 추천")
                        .subtitle("놓칠 수 없는 명작 아니메")
                        .imageUrl("/f4a3nB002i24I22B7sS1SgVvfe2.jpg") // 진격의 거인
                        .linkUrl("/api/v1/media/charts/JAPANESE_ANIME_SERIES")
                        .displayOrder(17)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("가슴 따뜻한 로맨스")
                        .subtitle("사랑이 필요한 당신을 위한 영화")
                        .imageUrl("/d5NXSklXo0qyIY2VFIh5bQJzgx.jpg") // 노트북
                        .linkUrl("/api/v1/media/charts/ROMANCE_CLASSICS")
                        .displayOrder(18)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("우주를 향한 위대한 여정")
                        .subtitle("광활한 우주를 배경으로 한 SF 대작")
                        .imageUrl("/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg") // 인터스텔라
                        .linkUrl("/api/v1/media/charts/SPACE_OPERA")
                        .displayOrder(19)
                        .isActive(false)
                        .build(),
                Banner.builder()
                        .title("왓챠 오리지널 시리즈")
                        .subtitle("새롭고 신선한 왓챠의 발견")
                        .imageUrl("/2w5q3pS7N222sO4p2i8S5yT1a5h.jpg") // 좋은 사람
                        .linkUrl("/api/v1/media/charts/platforms/WATCHA")
                        .displayOrder(20)
                        .isActive(false)
                        .build()
        );

        bannerRepository.saveAll(banners);
        log.info("더미 배너 {}개를 생성을 완료했습니다.", banners.size());
    }
}