package com.example.CineHive.datasync.domain.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderType {
    UNKNOWN(0, "Unknown"),
    FEMALE(1, "Female"),
    MALE(2, "Male"),
    NONBINARY(3, "Non-binary");

    private final int tmdbId;
    private final String description;

    // TMDB ID를 키로, GenderType Enum을 값으로 갖는 Map을 미리 생성하여 조회 성능을 높입니다.
    private static final Map<Integer, GenderType> TMDB_ID_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(GenderType::getTmdbId, Function.identity()));

    /**
     * TMDB API가 반환하는 숫자 ID를 기반으로 해당하는 GenderType Enum 상수를 찾습니다.
     * @param tmdbId TMDB에서 사용하는 성별 ID (0, 1, 2, 3)
     * @return 해당하는 GenderType Enum 상수. 일치하는 ID가 없으면 UNKNOWN을 반환합니다.
     */
    public static GenderType fromTmdbId(int tmdbId) {
        return TMDB_ID_MAP.getOrDefault(tmdbId, UNKNOWN);
    }
}