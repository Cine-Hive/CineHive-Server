package com.example.CineHive.entity.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TMDB의 영화 및 TV 장르를 통합하여 관리하는 Enum입니다.
 * 각 장르는 고유 ID, 한글 이름, 그리고 적용되는 미디어 타입(MOVIE, TV, 또는 둘 다)을 가집니다.
 */
@Getter
@RequiredArgsConstructor
public enum Genre {

    // --- 공통 또는 영화/TV에 따라 이름이 약간 다른 장르 ---
    ACTION(28, "액션", EnumSet.of(MediaType.MOVIE)),
    ACTION_ADVENTURE(10759, "액션 & 어드벤처", EnumSet.of(MediaType.TV)),
    ADVENTURE(12, "모험", EnumSet.of(MediaType.MOVIE)),
    ANIMATION(16, "애니메이션", EnumSet.of(MediaType.MOVIE, MediaType.TV)),
    COMEDY(35, "코미디", EnumSet.of(MediaType.MOVIE, MediaType.TV)),
    CRIME(80, "범죄", EnumSet.of(MediaType.MOVIE, MediaType.TV)),
    DOCUMENTARY(99, "다큐멘터리", EnumSet.of(MediaType.MOVIE, MediaType.TV)),
    DRAMA(18, "드라마", EnumSet.of(MediaType.MOVIE, MediaType.TV)),
    FAMILY(10751, "가족", EnumSet.of(MediaType.MOVIE, MediaType.TV)),
    FANTASY(14, "판타지", EnumSet.of(MediaType.MOVIE)),
    HISTORY(36, "역사", EnumSet.of(MediaType.MOVIE)),
    HORROR(27, "공포", EnumSet.of(MediaType.MOVIE)),
    MUSIC(10402, "음악", EnumSet.of(MediaType.MOVIE)),
    MYSTERY(9648, "미스터리", EnumSet.of(MediaType.MOVIE, MediaType.TV)),
    ROMANCE(10749, "로맨스", EnumSet.of(MediaType.MOVIE)),
    SCIENCE_FICTION(878, "SF", EnumSet.of(MediaType.MOVIE)),
    THRILLER(53, "스릴러", EnumSet.of(MediaType.MOVIE)),
    WAR(10752, "전쟁", EnumSet.of(MediaType.MOVIE)),
    WESTERN(37, "서부", EnumSet.of(MediaType.MOVIE, MediaType.TV)),

    // --- 영화에만 있는 장르 ---
    TV_MOVIE(10770, "TV 영화", EnumSet.of(MediaType.MOVIE)),

    // --- TV에만 있는 장르 ---
    KIDS(10762, "키즈", EnumSet.of(MediaType.TV)),
    NEWS(10763, "뉴스", EnumSet.of(MediaType.TV)),
    REALITY(10764, "리얼리티", EnumSet.of(MediaType.TV)),
    SCI_FI_FANTASY(10765, "SF & 판타지", EnumSet.of(MediaType.TV)),
    SOAP(10766, "소프 오페라", EnumSet.of(MediaType.TV)),
    TALK(10767, "토크", EnumSet.of(MediaType.TV)),
    WAR_POLITICS(10768, "전쟁 & 정치", EnumSet.of(MediaType.TV));


    private final int id;
    private final String koreanName;
    private final Set<MediaType> mediaTypes;

    /**
     * 특정 미디어 타입에 해당하는 모든 장르를 Set으로 반환합니다.
     *
     * @param mediaType 원하는 미디어 타입 (MOVIE 또는 TV)
     * @return 해당 미디어 타입에 속하는 Genre의 Set
     */
    public static Set<Genre> getGenresFor(MediaType mediaType) {
        return Arrays.stream(Genre.values())
                .filter(genre -> genre.getMediaTypes().contains(mediaType))
                .collect(Collectors.toSet());
    }

    /**
     * TMDB 장르 ID로 해당하는 Genre Enum을 찾습니다.
     *
     * @param id 찾으려는 장르의 TMDB ID
     * @return ID에 해당하는 Genre Enum을 담은 Optional 객체
     */
    public static Optional<Genre> findById(int id) {
        return Arrays.stream(Genre.values())
                .filter(genre -> genre.getId() == id)
                .findFirst();
    }
}