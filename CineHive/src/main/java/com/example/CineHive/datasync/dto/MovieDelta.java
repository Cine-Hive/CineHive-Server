package com.example.CineHive.datasync.dto;

import com.example.CineHive.datasync.domain.entity.*;
import java.util.List;

/**
 * ItemProcessor에서 ItemWriter로 변환된 영화 데이터 묶음을 전달하기 위한 데이터 캐리어(DTO).
 * Java Record를 사용하여 불변 객체로 간결하게 정의.
 */
public record MovieDelta(
        Movie movie,
        List<MovieGenre> genres,
        List<MovieKeyword> keywords,
        List<MovieCast> cast,
        List<MovieCrew> crew,
        Collection collection,
        List<ProductionCompany> companies,
        List<MovieProductionCompany> movieCompanies
) {}