package com.example.CineHive.datasync.dto;

import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.datasync.domain.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * ItemProcessor에서 ItemWriter로 변환된 영화 데이터 묶음을 전달하기 위한 데이터 캐리어(DTO).
 * Java Record를 사용하여 불변 객체로 간결하게 정의.
 */
@Slf4j
public record MovieDelta(
        Movie movie,
        Collection collection,
        List<ProductionCompany> companies,
        List<MovieGenre> genres,
        List<MovieKeyword> keywords,
        List<MovieCast> cast,
        List<MovieCrew> crew,
        List<MovieProductionCompany> movieCompanies
) {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * TMDB API 응답을 MovieDelta로 변환하는 static factory 메서드
     */
    public static MovieDelta fromTmdbResponse(TmdbMovieDetailResponse response) {
        Movie movie = Movie.fromTmdbResponse(response);
        Collection collection = response.collection() != null ? 
            Collection.fromTmdbResponse(response.collection()) : null;
        
        List<ProductionCompany> companies = response.productionCompanies() != null ?
            response.productionCompanies().stream()
                .map(ProductionCompany::fromTmdbResponse)
                .toList() : List.of();
        
        List<MovieGenre> genres = response.genres() != null ?
            response.genres().stream()
                .map(genre -> MovieGenre.of(response.id(), genre.id().longValue()))
                .toList() : List.of();
        
        List<MovieKeyword> keywords = response.keywords() != null && response.keywords().keywords() != null ?
            response.keywords().keywords().stream()
                .map(keyword -> MovieKeyword.of(response.id(), keyword.id()))
                .toList() : List.of();
        
        List<MovieCast> cast = response.credits() != null && response.credits().cast() != null ?
            IntStream.range(0, response.credits().cast().size())
                .mapToObj(i -> MovieCast.fromTmdbResponse(response.id(), response.credits().cast().get(i), i))
                .toList() : List.of();
        
        List<MovieCrew> crew = response.credits() != null && response.credits().crew() != null ?
            response.credits().crew().stream()
                .map(crewMember -> MovieCrew.fromTmdbResponse(response.id(), crewMember))
                .toList() : List.of();
        
        List<MovieProductionCompany> movieCompanies = response.productionCompanies() != null ?
            response.productionCompanies().stream()
                .map(company -> MovieProductionCompany.of(response.id(), company.id()))
                .toList() : List.of();

        return new MovieDelta(movie, collection, companies, genres, keywords, cast, crew, movieCompanies);
    }

    /**
     * 날짜 문자열을 LocalDate로 파싱하는 유틸리티 메서드
     */
    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {}", dateString);
            return null;
        }
    }

    /**
     * Double을 BigDecimal로 변환하는 유틸리티 메서드
     */
    private static BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}