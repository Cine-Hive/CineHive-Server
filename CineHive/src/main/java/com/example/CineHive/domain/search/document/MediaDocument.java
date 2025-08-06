package com.example.CineHive.domain.search.document;

import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.client.tmdb.dto.TmdbTvSeriesDetailResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch에 저장될 미디어 문서를 정의하는 클래스입니다.
 * TMDB의 원천 데이터와 CineHive의 커뮤니티 데이터를 모두 포함합니다.
 */
@Getter
@Builder
@Document(indexName = "media")
@Setting(settingPath = "elasticsearch/nori-settings.json")
public class MediaDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long tmdbId;

    @Field(type = FieldType.Keyword)
    private String mediaType;

    @Field(type = FieldType.Search_As_You_Type, analyzer = "nori_analyzer")
    private String title;

    @CompletionField(analyzer = "nori_analyzer", searchAnalyzer = "nori_analyzer")
    private String title_suggest;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String overview;

    @Field(type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private List<String> cast;

    @Field(type = FieldType.Keyword, index = false)
    private String posterPath;

    @Field(type = FieldType.Date)
    private String releaseDate;

    @Field(type = FieldType.Integer)
    private int likeCount;

    @Field(type = FieldType.Integer)
    private int reviewCount;

    @Field(type = FieldType.Double)
    private double avgRating;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

    @Field(type = FieldType.Boolean)
    private boolean adult;

    /**
     * TMDB 영화 DTO를 MediaDocument로 변환합니다. (배치 작업에서 사용)
     */
    public static MediaDocument from(TmdbMovieDetailResponse tmdb) {
        return MediaDocument.builder()
                .id(tmdb.id())
                .tmdbId(tmdb.id())
                .mediaType("MOVIE")
                .title(tmdb.title())
                .title_suggest(tmdb.title())
                .overview(tmdb.overview())
                .genres(tmdb.genres().stream().map(g -> g.name()).collect(Collectors.toList()))
                .cast(tmdb.credits().cast().stream().map(c -> c.name()).limit(5).collect(Collectors.toList())) // 상위 5명
                .posterPath(tmdb.posterPath())
                .releaseDate(tmdb.releaseDate())
                .likeCount(0)
                .reviewCount(0)
                .avgRating(0.0)
                .updatedAt(Instant.now())
                .adult(tmdb.adult())
                .build();
    }

    /**
     * TMDB TV 시리즈 DTO를 MediaDocument로 변환합니다. (배치 작업에서 사용)
     */
    public static MediaDocument from(TmdbTvSeriesDetailResponse tmdb) {
        return MediaDocument.builder()
                .id(tmdb.id())
                .tmdbId(tmdb.id())
                .mediaType("TV")
                .title(tmdb.name())
                .title_suggest(tmdb.name())
                .overview(tmdb.overview())
                .genres(tmdb.genres().stream().map(g -> g.name()).collect(Collectors.toList()))
                .cast(tmdb.credits().cast().stream().map(c -> c.name()).limit(5).collect(Collectors.toList())) // 상위 5명
                .posterPath(tmdb.posterPath())
                .releaseDate(tmdb.firstAirDate())
                .likeCount(0)
                .reviewCount(0)
                .avgRating(0.0)
                .updatedAt(Instant.now())
                .adult(tmdb.adult())
                .build();
    }
}