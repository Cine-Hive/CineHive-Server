package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.*;
import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.*;
import com.example.CineHive.mapper.MediaMapper;
import com.example.CineHive.repository.media.*;
import com.example.CineHive.util.TmdbUrlBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * TMDB API와 통신하여 미디어 데이터를 동기화하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaSyncServiceImpl implements MediaSyncService {

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

    @PostConstruct
    private void initWebClient() {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    /**
     * 미디어 데이터 동기화 (기본값 5페이지)
     */
    @Override
    @Transactional
    public void syncMediaData(Media.MediaType mediaType, Media.MediaCategory category) {
        // 기본 5페이지 호출
        syncMediaData(mediaType, category, 5);
    }

    /**
     * 미디어 데이터 동기화 (페이지 수 지정)
     */
    @Override
    @Transactional
    public void syncMediaData(Media.MediaType mediaType, Media.MediaCategory category, int maxPages) {
        log.info("미디어 데이터 동기화 시작: 타입={}, 카테고리={}, 페이지={}", mediaType, category, maxPages);
        
        for (int page = 1; page <= maxPages; page++) {
            // 미디어 타입별로 적절한 URL 생성
            String apiUrl;
            if (mediaType == Media.MediaType.MOVIE) {
                apiUrl = urlBuilder.movieList(getCategoryPath(category, mediaType), page);
            } else if (mediaType == Media.MediaType.TV) {
                apiUrl = urlBuilder.tvList(getCategoryPath(category, mediaType), page);
            } else { // ANIMATION
                String sortBy = getAnimationSortBy(category);
                apiUrl = urlBuilder.animationList(sortBy, page);
            }
            
            // API 호출 및 데이터 처리
            processApiResponse(apiUrl, mediaType, category);
        }
        
        log.info("미디어 데이터 동기화 완료: 타입={}, 카테고리={}, 페이지={}", mediaType, category, maxPages);
    }
    
    /**
     * 특정 미디어 데이터 동기화 (단일 미디어 ID)
     */
    @Override
    @Transactional
    public boolean syncSingleMedia(Long mediaId, Media.MediaType mediaType) {
        log.info("단일 미디어 데이터 동기화 시작: ID={}, 타입={}", mediaId, mediaType);
        
        try {
            // 기본 카테고리 설정 (개별 미디어 조회시 없어도 되지만 저장을 위해 필요)
            Media.MediaCategory defaultCategory = 
                (mediaType == Media.MediaType.TV) ? Media.MediaCategory.POPULAR : Media.MediaCategory.POPULAR;
            
            // 미디어 상세 정보 조회 및 저장
            fetchAndSaveMediaDetails(mediaId, mediaType, defaultCategory);
            
            log.info("단일 미디어 데이터 동기화 완료: ID={}, 타입={}", mediaId, mediaType);
            return true;
        } catch (Exception e) {
            log.error("단일 미디어 데이터 동기화 실패: ID={}, 타입={}", mediaId, mediaType, e);
            return false;
        }
    }
    
    /**
     * 애니메이션 정렬 방식 결정
     */
    private String getAnimationSortBy(Media.MediaCategory category) {
        return switch (category) {
            case POPULAR -> "popularity.desc";
            case TOP_RATED -> "vote_average.desc";
            case NOW_PLAYING -> "primary_release_date.desc";
            case UPCOMING -> "primary_release_date.asc";
            default -> "popularity.desc";
        };
    }
    
    /**
     * API 응답 처리
     */
    private void processApiResponse(String uriString, Media.MediaType mediaType, Media.MediaCategory category) {
        try {
            // TMDB API 호출
            String response = fetchFromTmdbApi(uriString);
            
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode resultsNode = rootNode.path("results");
                
                log.info("API 응답 처리 중: {} 개의 결과", resultsNode.size());
                
                for (JsonNode mediaNode : resultsNode) {
                    // ID 추출
                    Long mediaId = mediaNode.get("id").asLong();
                    
                    // JSON을 DTO로 변환
                    MediaItemDto dto = mediaMapper.mapJsonToMediaItemDto(mediaNode, mediaType);
                    
                    // 상세 정보 가져오기
                    fetchAndSaveMediaDetails(mediaId, mediaType, category);
                }
            }
        } catch (Exception e) {
            log.error("API 응답 처리 중 오류 발생", e);
            throw new RuntimeException("API 응답 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 미디어 상세 정보 조회 및 저장 (상세 정보, 출연진, 제작진, 비디오 등)
     */
    private void fetchAndSaveMediaDetails(Long id, Media.MediaType mediaType, Media.MediaCategory category) {
        try {
            // 미디어 유형에 맞는 경로 설정
            String path = getMediaTypePath(mediaType);
            
            // 상세 정보 URL 생성 및 API 호출
            String detailsUri = urlBuilder.mediaDetail(path, id);
            String response = fetchFromTmdbApi(detailsUri);
            
            if (response != null) {
                JsonNode detailsNode = objectMapper.readTree(response);
                
                // 애니메이션 여부 확인 및 미디어 타입 재결정
                Media.MediaType effectiveMediaType = mediaType;
                
                // 장르 목록에 16(애니메이션)이 포함되어 있으면 애니메이션으로 처리
                if (isAnimationGenre(detailsNode)) {
                    effectiveMediaType = Media.MediaType.ANIMATION;
                    log.info("미디어 ID {}는 장르 16(애니메이션)이 있어 ANIMATION으로 처리됩니다", id);
                }
                
                // JSON을 DTO로 변환
                MediaItemDto dto = mediaMapper.mapJsonToMediaItemDto(detailsNode, effectiveMediaType);
                
                // 상세 정보 저장
                saveMediaEntity(dto, category, effectiveMediaType);
                
                // 비디오 정보 추출 및 저장
                List<VideoDto> videoDtos = mediaMapper.extractVideosFromJson(detailsNode);
                if (!videoDtos.isEmpty()) {
                    saveVideos(videoDtos, id, effectiveMediaType);
                }
                
                // 출연진 정보 추출 및 저장
                List<CastDto> castDtos = mediaMapper.extractCastFromJson(detailsNode);
                if (!castDtos.isEmpty()) {
                    saveCast(castDtos, id, effectiveMediaType);
                }
                
                // 제작진 정보 추출 및 저장
                List<CrewDto> crewDtos = mediaMapper.extractCrewFromJson(detailsNode);
                if (!crewDtos.isEmpty()) {
                    saveCrew(crewDtos, id, effectiveMediaType);
                }
                
                log.info("미디어 상세 정보 저장 완료: ID={}, 타입={}", id, effectiveMediaType);
            }
        } catch (Exception e) {
            log.error("미디어 상세 정보 처리 중 오류 발생: ID={}", id, e);
        }
    }
    
    /**
     * 애니메이션 장르 여부 확인 (장르 ID 16 포함 여부)
     */
    private boolean isAnimationGenre(JsonNode mediaNode) {
        if (mediaNode.has("genres")) {
            JsonNode genresNode = mediaNode.get("genres");
            for (JsonNode genreNode : genresNode) {
                if (genreNode.get("id").asInt() == 16) { // 16은 애니메이션 장르 ID
                    return true;
                }
            }
        } else if (mediaNode.has("genre_ids")) {
            JsonNode genreIdsNode = mediaNode.get("genre_ids");
            for (JsonNode genreIdNode : genreIdsNode) {
                if (genreIdNode.asInt() == 16) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 미디어 엔티티 저장
     */
    @Transactional
    private void saveMediaEntity(MediaItemDto dto, Media.MediaCategory category, Media.MediaType mediaType) {
        switch (mediaType) {
            case MOVIE -> {
                Movie movie = mediaMapper.mapDtoToMovieEntity(dto, category);
                movieRepository.save(movie);
            }
            case TV -> {
                Tv tv = mediaMapper.mapDtoToTvEntity(dto, category);
                tvRepository.save(tv);
            }
            case ANIMATION -> {
                Animation animation = mediaMapper.mapDtoToAnimationEntity(dto, category);
                animationRepository.save(animation);
            }
        }
    }
    
    /**
     * 출연진 정보 저장
     */
    @Transactional
    private void saveCast(List<CastDto> castDtos, Long mediaId, Media.MediaType mediaType) {
        for (CastDto dto : castDtos) {
            Cast cast = mediaMapper.mapDtoToCastEntity(dto, mediaId, mediaType);
            castRepository.save(cast);
        }
    }
    
    /**
     * 제작진 정보 저장
     */
    @Transactional
    private void saveCrew(List<CrewDto> crewDtos, Long mediaId, Media.MediaType mediaType) {
        for (CrewDto dto : crewDtos) {
            Crew crew = mediaMapper.mapDtoToCrewEntity(dto, mediaId, mediaType);
            crewRepository.save(crew);
        }
    }
    
    /**
     * 비디오 정보 저장
     */
    @Transactional
    private void saveVideos(List<VideoDto> videoDtos, Long mediaId, Media.MediaType mediaType) {
        for (VideoDto dto : videoDtos) {
            Video video = mediaMapper.mapDtoToVideoEntity(dto, mediaId, mediaType);
            videoRepository.save(video);
        }
    }
    
    /**
     * TMDB API 호출
     */
    private String fetchFromTmdbApi(String uri) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
    
    /**
     * 미디어 타입별 API 경로 
     */
    private String getMediaTypePath(Media.MediaType mediaType) {
        return switch (mediaType) {
            case MOVIE -> "movie";
            case TV -> "tv";
            case ANIMATION -> "movie"; // 애니메이션은 TMDB에서는 movie나 tv로 조회 필요
            default -> "movie";
        };
    }
    
    /**
     * 카테고리별 API 경로
     */
    private String getCategoryPath(Media.MediaCategory category, Media.MediaType mediaType) {
        if (mediaType == Media.MediaType.MOVIE) {
            return switch (category) {
                case POPULAR -> "popular";
                case TOP_RATED -> "top_rated";
                case NOW_PLAYING -> "now_playing";
                case UPCOMING -> "upcoming";
                default -> "popular";
            };
        } else if (mediaType == Media.MediaType.TV) {
            return switch (category) {
                case POPULAR -> "popular";
                case TOP_RATED -> "top_rated";
                case ON_THE_AIR -> "on_the_air";
                case AIRING_TODAY -> "airing_today";
                default -> "popular";
            };
        } else { // ANIMATION
            return "movie"; // 애니메이션은 discover/movie 엔드포인트 사용
        }
    }
} 