package com.example.CineHive.domain.person.service;

import com.example.CineHive.domain.person.dto.FilmographyResponse;
import com.example.CineHive.domain.person.dto.PersonDetailsResponse;
import com.example.CineHive.domain.person.dto.PersonInListResponse;
import com.example.CineHive.global.dto.SliceResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonQueryService {

    PersonDetailsResponse getPersonDetails(Long personTmdbId);

    /**
     * 인기 인물 목록을 슬라이스하여 조회합니다.
     * @param pageable 페이징 정보 (page, size)
     * @return SliceResponse<PersonInListResponse>
     */
    SliceResponse<PersonInListResponse> getPopularPeople(Pageable pageable);

    /**
     * 특정 인물의 필모그래피(참여 작품 목록)를 슬라이스하여 조회합니다.
     * @param personTmdbId 조회할 인물의 TMDB ID
     * @param pageable 페이징 정보
     * @return SliceResponse<FilmographyResponse> 페이징된 작품 목록
     */
    SliceResponse<FilmographyResponse> getFilmography(Long personTmdbId, Pageable pageable);
}