package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
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
public class TvKeyword extends BaseEntity {

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
    
    /**
     * TV ID와 Keyword ID로 TvKeyword 엔티티를 생성하는 static factory 메서드
     */
    public static TvKeyword of(Long tvId, Long keywordId) {
        return TvKeyword.builder()
                .tvId(tvId)
                .keywordId(keywordId)
                .build();
    }
}