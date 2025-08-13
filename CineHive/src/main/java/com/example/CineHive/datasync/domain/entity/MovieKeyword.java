package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncMovieKeyword")
@Table(name = "movie_keyword")
@IdClass(MovieKeywordId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieKeyword extends BaseEntity {

    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @Id
    @Column(name = "keyword_id")
    private Long keywordId;

    @Builder
    public MovieKeyword(Long movieId, Long keywordId) {
        this.movieId = movieId;
        this.keywordId = keywordId;
    }

    /**
     * 영화 ID와 키워드 ID로 MovieKeyword 엔티티를 생성하는 static factory 메서드
     */
    public static MovieKeyword of(Long movieId, Long keywordId) {
        return MovieKeyword.builder()
                .movieId(movieId)
                .keywordId(keywordId)
                .build();
    }
}