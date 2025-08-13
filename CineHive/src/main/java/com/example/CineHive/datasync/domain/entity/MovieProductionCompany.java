package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncMovieProductionCompany")
@Table(name = "movie_production_company")
@IdClass(MovieProductionCompanyId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieProductionCompany extends BaseEntity {

    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @Id
    @Column(name = "company_id")
    private Long companyId;

    @Builder
    public MovieProductionCompany(Long movieId, Long companyId) {
        this.movieId = movieId;
        this.companyId = companyId;
    }

    /**
     * 영화 ID와 제작사 ID로 MovieProductionCompany 엔티티를 생성하는 static factory 메서드
     */
    public static MovieProductionCompany of(Long movieId, Long companyId) {
        return MovieProductionCompany.builder()
                .movieId(movieId)
                .companyId(companyId)
                .build();
    }
}