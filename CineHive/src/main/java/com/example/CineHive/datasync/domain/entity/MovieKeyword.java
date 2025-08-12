package com.example.CineHive.datasync.domain.entity;

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
public class MovieKeyword {

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
}