package com.example.CineHive.domain.media.controller.entity;

import lombok.Getter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum Genre {
    ACTION(28, "액션"),
    ADVENTURE(12, "모험"),
    ANIMATION(16, "애니메이션"),
    COMEDY(35, "코미디"),
    CRIME(80, "범죄"),
    DOCUMENTARY(99, "다큐멘터리"),
    DRAMA(18, "드라마"),
    FAMILY(10751, "가족"),
    FANTASY(14, "판타지"),
    HISTORY(36, "역사"),
    HORROR(27, "공포"),
    MUSIC(10402, "음악"),
    MYSTERY(9648, "미스터리"),
    ROMANCE(10749, "로맨스"),
    SCIENCE_FICTION(878, "SF"),
    TV_MOVIE(10770, "TV 영화"),
    THRILLER(53, "스릴러"),
    WAR(10752, "전쟁"),
    WESTERN(37, "서부"),
    // TV 전용 장르
    ACTION_ADVENTURE(10759, "액션 & 어드벤처"),
    KIDS(10762, "키즈"),
    NEWS(10763, "뉴스"),
    REALITY(10764, "리얼리티"),
    SCI_FI_FANTASY(10765, "SF & 판타지"),
    SOAP(10766, "소프"),
    TALK(10767, "토크"),
    WAR_POLITICS(10768, "전쟁 & 정치");

    private final int tmdbId;
    private final String koreanName;

    private static final Map<Integer, Genre> TMDB_ID_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(Genre::getTmdbId, Function.identity()));

    Genre(int tmdbId, String koreanName) {
        this.tmdbId = tmdbId;
        this.koreanName = koreanName;
    }

    /**
     * TMDB 장르 ID를 기반으로 해당하는 Genre Enum 상수를 찾습니다.
     * @param tmdbId TMDB에서 사용하는 장르의 고유 ID
     * @return 해당하는 Genre Enum 상수 (Optional)
     */
    public static Optional<Genre> fromTmdbId(int tmdbId) {
        return Optional.ofNullable(TMDB_ID_MAP.get(tmdbId));
    }
}
