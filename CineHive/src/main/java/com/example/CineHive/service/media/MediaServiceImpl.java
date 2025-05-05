package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.*;
import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.*;
import com.example.CineHive.exception.GenreNotFoundException;
import com.example.CineHive.mapper.MediaMapper;
import com.example.CineHive.repository.media.*;
import com.example.CineHive.util.TmdbUrlBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 미디어 정보를 관리하고 제공하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    // API 키
    @Value("${tmdb.api.key}")
    private String apiKey;

    // WebClient 관련
    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    
    // JSON 처리
    private final ObjectMapper objectMapper;
    
    // URL 빌더
    private final TmdbUrlBuilder urlBuilder;
    
    // 매퍼
    private final MediaMapper mediaMapper;
    
    // 리포지토리
    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final AnimationRepository animationRepository;
    private final VideoRepository videoRepository;
    private final CastRepository castRepository;
    private final CrewRepository crewRepository;
    private final MediaGenreRepository mediaGenreRepository;
    private final GenreRepository genreRepository;

    @PostConstruct
    private void initWebClient() {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    /**
     * 미디어 ID로 상세 정보 조회
     */
    @Override
    @Transactional(readOnly = true)
    public MediaDetailsDto getMediaDetails(Long mediaId, Media.MediaType mediaType) {
        Media media = getMediaById(mediaId, mediaType);
        if (media == null) {
            log.warn("해당 ID의 미디어를 찾을 수 없음: {}, 타입: {}", mediaId, mediaType);
            return null;
        }
        
        // 비디오 목록 조회
        List<Video> videos = videoRepository.findByMediaIdAndMediaType(mediaId, mediaType);
        
        // 출연진/제작진 조회
        List<Cast> casts = castRepository.findByMediaIdAndMediaType(mediaId, mediaType);
        List<Crew> crews = crewRepository.findByMediaIdAndMediaType(mediaId, mediaType);
        
        // DTO 변환 및 반환
        return mediaMapper.toMediaDetailsDto(media, videos, casts, crews);
    }
    
    /**
     * 카테고리별 미디어 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<MediaItemDto> getMediaList(Media.MediaType mediaType, Media.MediaCategory category) {
        List<Media> mediaList = new ArrayList<>();
        
        // 미디어 타입별 리포지토리 호출
        switch (mediaType) {
            case MOVIE -> mediaList.addAll(movieRepository.findAllByCategory(category));
            case TV -> mediaList.addAll(tvRepository.findAllByCategory(category));
            case ANIMATION -> mediaList.addAll(animationRepository.findAllByCategory(category));
        }
        
        // DTO 변환 및 반환
        return mediaList.stream()
                .map(media -> mediaMapper.toMediaItemDto(media))
                .toList();
    }
    
    /**
     * 카테고리별 미디어 목록 조회 (페이지네이션)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MediaItemDto> getMediaListPaged(Media.MediaType mediaType, Media.MediaCategory category, Pageable pageable) {
        log.info("페이징된 {} 미디어 목록 조회: 카테고리={}, 페이지={}, 사이즈={}", 
                 mediaType, category, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<? extends Media> mediaPage;
        
        // 미디어 타입별 리포지토리 호출
        switch (mediaType) {
            case MOVIE -> {
                mediaPage = movieRepository.findAllByCategory(category, pageable);
            }
            case TV -> {
                mediaPage = tvRepository.findAllByCategory(category, pageable);
            }
            case ANIMATION -> {
                mediaPage = animationRepository.findAllByCategory(category, pageable);
            }
            default -> {
                log.warn("지원하지 않는 미디어 타입: {}", mediaType);
                return Page.empty(pageable);
            }
        }
        
        // DTO 변환
        List<MediaItemDto> dtoList = mediaPage.getContent().stream()
                .map(media -> mediaMapper.toMediaItemDto(media))
                .toList();
        
        // 페이지 정보 유지하면서 DTO로 변환한 Page 반환
        return new PageImpl<>(dtoList, pageable, mediaPage.getTotalElements());
    }
    
    /**
     * 미디어 제목 검색
     */
    @Override
    @Transactional(readOnly = true)
    public List<MediaItemDto> searchMedia(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String searchKeyword = keyword.trim().toLowerCase();
        
        List<Media> results = new ArrayList<>();
        results.addAll(movieRepository.findAllByTitleContainingIgnoreCase(searchKeyword));
        results.addAll(tvRepository.findAllByTitleContainingIgnoreCase(searchKeyword));
        results.addAll(animationRepository.findAllByTitleContainingIgnoreCase(searchKeyword));
        
        log.info("검색어 '{}' 제목 검색 결과: {} 건", searchKeyword, results.size());
        
        // 검색 결과가 없으면 빈 목록 반환
        if (results.isEmpty()) {
            return List.of();
        }
        
        // 인기도 기준 내림차순 정렬
        results.sort(Comparator.comparing(Media::getPopularity).reversed());
        
        // DTO 변환 및 반환
        return results.stream()
                .map(media -> mediaMapper.toMediaItemDto(media))
                .toList();
    }
    
    /**
     * 특정 장르의 미디어 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<MediaItemDto> getMediaListByGenre(Integer genreId, Media.MediaType mediaType) {
        log.info("장르 ID: {}, 미디어 타입: {}에 해당하는 미디어 목록 조회", genreId, mediaType);
        
        // 장르 존재 여부 확인
        Optional<Genre> genreOpt = genreRepository.findById(genreId);
        if (genreOpt.isEmpty()) {
            log.warn("존재하지 않는 장르 ID: {}", genreId);
            throw new GenreNotFoundException("장르 ID: " + genreId + "에 해당하는 장르를 찾을 수 없습니다.");
        }
        
        // 애니메이션 장르(16) 특별 처리
        if (genreId == 16) {
            log.info("애니메이션 장르(16) 요청 - 모든 애니메이션 반환");
            return getMediaList(Media.MediaType.ANIMATION, Media.MediaCategory.DEFAULT);
        }
        
        // 미디어 타입이 ANIMATION이면 Animation 테이블에서만 조회
        if (mediaType == Media.MediaType.ANIMATION) {
            // 장르별 미디어 ID 목록 조회 (ANIMATION 타입만)
            List<MediaGenre> mediaGenres = mediaGenreRepository.findByGenreIdAndMediaType(genreId, Media.MediaType.ANIMATION);
            
            if (mediaGenres.isEmpty()) {
                log.info("장르 ID: {}, 미디어 타입: ANIMATION에 해당하는 미디어가 없습니다", genreId);
                return List.of();
            }
            
            // 미디어 ID 추출
            List<Long> mediaIds = mediaGenres.stream()
                    .map(MediaGenre::getMediaId)
                    .collect(Collectors.toList());
            
            // Animation 엔티티 조회
            List<Animation> animations = animationRepository.findAllById(mediaIds);
            
            // 인기도 기준 내림차순 정렬
            animations.sort(Comparator.comparing(Media::getPopularity).reversed());
            
            // DTO 변환
            return animations.stream()
                    .map(mediaMapper::toMediaItemDto)
                    .collect(Collectors.toList());
        }
        
        // 장르별 미디어 ID 목록 조회
        List<MediaGenre> mediaGenres = mediaGenreRepository.findByGenreIdAndMediaType(genreId, mediaType);
        
        if (mediaGenres.isEmpty()) {
            log.info("장르 ID: {}, 미디어 타입: {}에 해당하는 미디어가 없습니다", genreId, mediaType);
            return List.of();
        }
        
        // 미디어 ID 추출
        List<Long> mediaIds = mediaGenres.stream()
                .map(MediaGenre::getMediaId)
                .collect(Collectors.toList());
        
        // 미디어 목록 조회
        List<Media> mediaList = new ArrayList<>();
        
        switch (mediaType) {
            case MOVIE -> 
                mediaList.addAll(movieRepository.findAllById(mediaIds));
            case TV -> 
                mediaList.addAll(tvRepository.findAllById(mediaIds));
            default -> {
                log.warn("지원하지 않는 미디어 타입: {}", mediaType);
                return List.of();
            }
        }
        
        // 인기도 기준 내림차순 정렬
        mediaList.sort(Comparator.comparing(Media::getPopularity).reversed());
        
        // DTO 변환
        return mediaList.stream()
                .map(mediaMapper::toMediaItemDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 장르의 미디어 목록 페이징 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MediaItemDto> getMediaListByGenrePaged(Integer genreId, Media.MediaType mediaType, Pageable pageable) {
        log.info("장르 ID: {}, 미디어 타입: {}에 해당하는 페이징된 미디어 목록 조회", genreId, mediaType);
        
        // 장르 존재 여부 확인
        Optional<Genre> genreOpt = genreRepository.findById(genreId);
        if (genreOpt.isEmpty()) {
            log.warn("존재하지 않는 장르 ID: {}", genreId);
            throw new GenreNotFoundException("장르 ID: " + genreId + "에 해당하는 장르를 찾을 수 없습니다.");
        }
        
        // 애니메이션 장르(16) 특별 처리
        if (genreId == 16) {
            log.info("애니메이션 장르(16) 요청 - 페이징된 모든 애니메이션 반환");
            return getMediaListPaged(Media.MediaType.ANIMATION, Media.MediaCategory.DEFAULT, pageable);
        }
        
        // 미디어 타입이 ANIMATION이면 Animation 테이블에서만 조회
        if (mediaType == Media.MediaType.ANIMATION) {
            // 장르별 미디어 ID 목록 페이징 조회 (ANIMATION 타입만)
            Page<MediaGenre> mediaGenrePage = mediaGenreRepository.findByGenreIdAndMediaType(
                    genreId, Media.MediaType.ANIMATION, pageable);
            
            if (mediaGenrePage.isEmpty()) {
                log.info("장르 ID: {}, 미디어 타입: ANIMATION에 해당하는 미디어가 없습니다", genreId);
                return Page.empty(pageable);
            }
            
            // 미디어 ID 추출
            List<Long> mediaIds = mediaGenrePage.getContent().stream()
                    .map(MediaGenre::getMediaId)
                    .collect(Collectors.toList());
            
            // Animation 엔티티 조회
            List<Animation> animations = animationRepository.findAllById(mediaIds);
            
            // 인기도 기준 내림차순 정렬
            animations.sort(Comparator.comparing(Media::getPopularity).reversed());
            
            // DTO 변환
            List<MediaItemDto> dtoList = animations.stream()
                    .map(mediaMapper::toMediaItemDto)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(dtoList, pageable, mediaGenrePage.getTotalElements());
        }
        
        // 장르별 미디어 ID 목록 페이징 조회
        Page<MediaGenre> mediaGenrePage = mediaGenreRepository.findByGenreIdAndMediaType(genreId, mediaType, pageable);
        
        if (mediaGenrePage.isEmpty()) {
            log.info("장르 ID: {}, 미디어 타입: {}에 해당하는 미디어가 없습니다", genreId, mediaType);
            return Page.empty(pageable);
        }
        
        // 미디어 ID 추출
        List<Long> mediaIds = mediaGenrePage.getContent().stream()
                .map(MediaGenre::getMediaId)
                .collect(Collectors.toList());
        
        // 미디어 목록 조회
        List<Media> mediaList = new ArrayList<>();
        
        switch (mediaType) {
            case MOVIE -> 
                mediaList.addAll(movieRepository.findAllById(mediaIds));
            case TV -> 
                mediaList.addAll(tvRepository.findAllById(mediaIds));
            default -> {
                log.warn("지원하지 않는 미디어 타입: {}", mediaType);
                return Page.empty(pageable);
            }
        }
        
        // 인기도 기준 내림차순 정렬
        mediaList.sort(Comparator.comparing(Media::getPopularity).reversed());
        
        // DTO 변환
        List<MediaItemDto> dtoList = mediaList.stream()
                .map(mediaMapper::toMediaItemDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, mediaGenrePage.getTotalElements());
    }
    
    /**
     * 미디어 ID와 타입으로 미디어 조회
     */
    private Media getMediaById(Long mediaId, Media.MediaType mediaType) {
        return switch (mediaType) {
            case MOVIE -> movieRepository.findById(mediaId).orElse(null);
            case TV -> tvRepository.findById(mediaId).orElse(null);
            case ANIMATION -> animationRepository.findById(mediaId).orElse(null);
        };
    }
} 