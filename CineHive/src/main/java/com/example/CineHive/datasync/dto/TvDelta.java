package com.example.CineHive.datasync.dto;

import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.datasync.domain.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.IntStream;

/**
 * ItemProcessor에서 ItemWriter로 변환된 TV 시리즈 데이터 묶음을 전달하기 위한 데이터 캐리어(DTO).
 * Java Record를 사용하여 불변 객체로 간결하게 정의.
 */
@Slf4j
public record TvDelta(
        TvSeries tvSeries,
        List<Network> networks,
        List<ProductionCompany> companies,
        List<Genre> genreEntities,
        List<Keyword> keywordEntities,
        List<TvGenre> genres,
        List<TvKeyword> keywords,
        List<TvCast> cast,
        List<TvCrew> crew,
        List<TvSeriesNetwork> tvNetworks,
        List<TvSeason> seasons
) {

    /**
     * TMDB API 응답을 TvDelta로 변환하는 static factory 메서드
     */
    public static TvDelta fromTmdbResponse(TmdbTvSeriesDetailResponse response) {
        TvSeries tvSeries = TvSeries.fromTmdbResponse(response);
        
        List<Network> networks = response.networks() != null ?
            response.networks().stream()
                .map(Network::fromTmdbResponse)
                .toList() : List.of();
        
        List<ProductionCompany> companies = response.productionCompanies() != null ?
            response.productionCompanies().stream()
                .map(ProductionCompany::fromTmdbResponse)
                .toList() : List.of();
        
        List<Genre> genreEntities = response.genres() != null ?
            response.genres().stream()
                .map(g -> Genre.builder()
                    .tmdbId(g.id().longValue())
                    .name(g.name())
                    .build())
                .toList() : List.of();
        
        List<Keyword> keywordEntities = response.keywords() != null && response.keywords().keywords() != null ?
            response.keywords().keywords().stream()
                .map(k -> Keyword.builder()
                    .tmdbId(k.id())
                    .name(k.name())
                    .build())
                .toList() : List.of();
        
        List<TvGenre> genres = response.genres() != null ?
            response.genres().stream()
                .map(genre -> TvGenre.of(response.id(), genre.id().longValue()))
                .toList() : List.of();
        
        List<TvKeyword> keywords = response.keywords() != null && response.keywords().keywords() != null ?
            response.keywords().keywords().stream()
                .map(keyword -> TvKeyword.of(response.id(), keyword.id()))
                .toList() : List.of();
        
        List<TvCast> cast = response.aggregateCredits() != null && response.aggregateCredits().cast() != null ?
            IntStream.range(0, response.aggregateCredits().cast().size())
                .mapToObj(i -> TvCast.fromTmdbResponse(response.id(), response.aggregateCredits().cast().get(i), i))
                .toList() : List.of();
        
        List<TvCrew> crew = response.aggregateCredits() != null && response.aggregateCredits().crew() != null ?
            response.aggregateCredits().crew().stream()
                .map(crewMember -> TvCrew.fromTmdbResponse(response.id(), crewMember))
                .toList() : List.of();
        
        List<TvSeriesNetwork> tvNetworks = response.networks() != null ?
            response.networks().stream()
                .map(network -> TvSeriesNetwork.of(response.id(), network.id()))
                .toList() : List.of();
        
        List<TvSeason> seasons = response.seasons() != null ?
            response.seasons().stream()
                .map(season -> TvSeason.fromTmdbResponse(response.id(), season))
                .toList() : List.of();

        return new TvDelta(tvSeries, networks, companies, genreEntities, keywordEntities, 
                          genres, keywords, cast, crew, tvNetworks, seasons);
    }
}