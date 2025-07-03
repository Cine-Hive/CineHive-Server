package com.example.CineHive.dto.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChartType {

    // 기본 및 트렌드 차트
    POPULAR_MOVIES("인기 영화"),
    TOP_RATED_MOVIES("평점 높은 영화"),
    UPCOMING_MOVIES("개봉 예정 영화"),
    NOW_PLAYING_MOVIES("현재 상영중인 영화"),
    TRENDING_MOVIES_WEEK("주간 트렌드 영화"),

    POPULAR_TV("인기 TV 시리즈"),
    TOP_RATED_TV("평점 높은 TV 시리즈"),
    ON_THE_AIR_TV("현재 방영중인 TV 시리즈"),
    AIRING_TODAY_TV("오늘 방영하는 TV 시리즈"),
    TRENDING_TV_WEEK("주간 트렌드 TV 시리즈"),

    // 장르별 명작 컬렉션
    ACTION_BLOCKBUSTERS("액션 블록버스터"),
    SCI_FI_MASTERPIECES("SF 명작"),
    THRILLER_MUST_WATCH("꼭 봐야 할 스릴러"),
    ROMANCE_CLASSICS("로맨스 명작"),
    HORROR_TOP_PICKS("공포 영화 추천"),
    COMEDY_FAVORITES("코미디 인기작"),
    DOCUMENTARY_ESSENTIALS("다큐멘터리 필람작"),
    CRIME_DRAMA_HITS("범죄 드라마 히트작 (TV)"),
    FANTASY_EPICS("판타지 서사시"),
    ANIMATION_FOR_ADULTS("성인용 애니메이션"),
    WAR_AND_HISTORY("전쟁 및 역사 영화"),
    MUSIC_AND_MUSICALS("음악 및 뮤지컬 영화"),

    // 제작사/스튜디오 특별관
    MARVEL_UNIVERSE_MOVIES("마블 시네마틱 유니버스"),
    PIXAR_ANIMATION_COLLECTION("픽사 애니메이션 컬렉션"),
    A24_FILMS_SELECTION("A24 스튜디오 작품선"),
    STUDIO_GHIBLI_MOVIES("스튜디오 지브리 컬렉션"),
    DC_UNIVERSE_MOVIES("DC 유니버스 영화"),
    BLUMHOUSE_HORROR("블룸하우스 공포 영화"),
    WARNER_BROS_ANIMATION("워너 브라더스 애니메이션"),

    // 국가별 인기 콘텐츠
    KOREAN_WAVE_MOVIES("한국 영화 인기작"),
    KOREAN_DRAMA_SERIES("명품 K-드라마"),
    JAPANESE_ANIME_SERIES("일본 애니메이션 시리즈"),
    BRITISH_DRAMA_SERIES("영국 드라마 시리즈"),
    FRENCH_CINEMA_SELECTION("프랑스 영화 추천"),
    INDIAN_BOLLYWOOD_HITS("인도 발리우드 히트작"),

    // 테마/키워드별 컬렉션
    TIME_TRAVEL_ADVENTURES("시간 여행 영화"),
    ZOMBIE_APOCALYPSE_SURVIVAL("좀비 아포칼립스"),
    CYBERPUNK_FUTURES("사이버펑크"),
    SUPERHERO_SHOWDOWN("슈퍼히어로 영화"),
    LEGAL_DRAMA_MOVIES("법정 드라마 영화"),
    SPY_THRILLER_COLLECTION("스파이 스릴러 컬렉션"),
    POST_APOCALYPSE("포스트 아포칼립스"),
    HIGH_SCHOOL_TEEN_MOVIES("하이틴 영화"),
    SPACE_OPERA("스페이스 오페라"),

    // 특별 TV 시리즈 컬렉션
    HBO_MASTERPIECE_SERIES("HBO 명작 시리즈"),
    NETFLIX_ORIGINAL_SERIES("넷플릭스 오리지널 시리즈"),
    APPLE_TV_ORIGINALS("애플TV+ 오리지널"),
    DISNEY_PLUS_ORIGINALS("디즈니+ 오리지널"),

    // 레트로/시대별 차트
    Y2K_MOVIES_POPULAR("2000년대 인기 영화"),
    BEST_OF_2000S_TV("2000년대 베스트 TV 시리즈"),

    // 시간 기반 차트
    RECENTLY_RELEASED_POPULAR("최근 1개월 내 공개작"),
    BEST_OF_2023_H2_TV("2023년 하반기 인기 TV 시리즈"),
    BEST_ANIMATION_OF_THE_YEAR("2023년 최고의 애니메이션"),

    // 특별 속성 기반 차트
    ONE_SEASON_WONDERS("한 시즌만 방영한 명작"),
    KOREAN_ACTORS_IN_HOLLYWOOD("한국 배우 출연 해외 영화");

    private final String description;

    public static ChartType fromString(String text) {
        return Arrays.stream(values())
                .filter(b -> b.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown chart type: " + text));
    }
}