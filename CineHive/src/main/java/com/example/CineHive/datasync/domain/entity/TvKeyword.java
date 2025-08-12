package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncTvKeyword")
@Table(name = "tv_keyword")
@IdClass(TvKeywordId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvKeyword {

    @Id
    @Column(name = "tv_id")
    private Long tvId;

    @Id
    @Column(name = "keyword_id")
    private Long keywordId;

    @Builder
    public TvKeyword(Long tvId, Long keywordId) {
        this.tvId = tvId;
        this.keywordId = keywordId;
    }
}