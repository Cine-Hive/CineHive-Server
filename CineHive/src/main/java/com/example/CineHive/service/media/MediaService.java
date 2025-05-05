package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.MediaDetailsDto;
import com.example.CineHive.dto.media.MediaItemDto;
import com.example.CineHive.entity.media.Media;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaService {
    /**
     * 미디어 ID로 상세 정보 조회
     * 
     * @param mediaId 미디어 ID
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @return 미디어 상세 정보
     */
    MediaDetailsDto getMediaDetails(Long mediaId, Media.MediaType mediaType);
    
    /**
     * 카테고리별 미디어 목록 조회
     * 
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @param category 카테고리 (인기, 평점 높은, 현재 상영 중 등)
     * @return 미디어 목록
     */
    List<MediaItemDto> getMediaList(Media.MediaType mediaType, Media.MediaCategory category);
    
    /**
     * 카테고리별 미디어 목록 조회 (페이지네이션)
     * 
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @param category 카테고리 (인기, 평점 높은, 현재 상영 중 등)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징된 미디어 목록
     */
    Page<MediaItemDto> getMediaListPaged(Media.MediaType mediaType, Media.MediaCategory category, Pageable pageable);
    
    /**
     * 미디어 검색
     * 
     * @param keyword 검색 키워드
     * @return 검색 결과 목록
     */
    List<MediaItemDto> searchMedia(String keyword);
    
    /**
     * 특정 장르의 미디어 목록 조회
     * 
     * @param genreId 장르 ID
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @return 해당 장르의 미디어 목록
     */
    List<MediaItemDto> getMediaListByGenre(Integer genreId, Media.MediaType mediaType);
    
    /**
     * 특정 장르의 미디어 목록 페이징 조회
     * 
     * @param genreId 장르 ID
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징된 장르별 미디어 목록
     */
    Page<MediaItemDto> getMediaListByGenrePaged(Integer genreId, Media.MediaType mediaType, Pageable pageable);
} 