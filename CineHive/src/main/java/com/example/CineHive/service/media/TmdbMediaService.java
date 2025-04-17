package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.MediaDto;
import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.MediaRecommendation;
import com.example.CineHive.entity.media.Video;
import com.example.CineHive.entity.media.Media;
import com.example.CineHive.repository.media.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TmdbMediaService implements MediaService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private MediaMapperService mediaMapperService;
    
    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private CastRepository castRepository;
    
    @Autowired
    private CrewRepository crewRepository;
    
    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private TvRepository tvRepository;
    
    @Autowired
    private AnimationRepository animationRepository;
    
    @Autowired
    private MediaRecommendationRepository mediaRecommendationRepository;

    private int accessCountThreshold = 3;
    private int expiryDays = 30;

    public TmdbMediaService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    @Override
    public MediaDto.MediaItemDto getMediaById(Media.MediaType mediaType, Long id) {
        String path = getMediaTypePath(mediaType);
        
        String response = webClient.get()
                .uri("/" + path + "/" + id + "?api_key=" + apiKey + "&language=ko-KR&append_to_response=videos,credits")
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                return mediaMapperService.mapJsonToMediaItemDto(rootNode, mediaType);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse media details", e);
            }
        }
        
        return null;
    }

    @Override
    public MediaDto searchMedia(Media.MediaType mediaType, String query, int page) {
        String path = getMediaTypePath(mediaType);
        
        String response = webClient.get()
                .uri("/search/" + path + "?api_key=" + apiKey + "&language=ko-KR&query=" + query + "&page=" + page)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        return parseMediaListResponse(response, mediaType);
    }

    @Override
    public MediaDto getMediaByCategory(Media.MediaType mediaType, Media.MediaCategory category, int page) {
        String path = getMediaTypePath(mediaType);
        String categoryPath = getCategoryPath(category, mediaType);
        
        String response = webClient.get()
                .uri("/" + path + "/" + categoryPath + "?api_key=" + apiKey + "&language=ko-KR&page=" + page)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        return parseMediaListResponse(response, mediaType);
    }

    @Override
    public MediaDto getSimilarMedia(Media.MediaType mediaType, Long id, int page) {
        // 1. 먼저 DB에서 캐싱된 추천 정보 확인
        List<MediaRecommendation> cachedRecommendations = 
            mediaRecommendationRepository.findByMediaIdAndMediaType(id, mediaType);
            
        if (!cachedRecommendations.isEmpty()) {
            // 저장된 추천 정보가 있으면 사용
            MediaDto result = buildMediaDtoFromRecommendations(cachedRecommendations, page);
            
            // 캐시 히트 통계 업데이트
            updateRecommendationStats(cachedRecommendations);
            
            if (result != null && !result.getResults().isEmpty()) {
                return result;
            }
            // 캐시에 데이터가 부족하면 API 호출로 보완
        }
        
        // 2. 캐시가 없거나 충분하지 않으면 TMDB API 호출
        String path = getMediaTypePath(mediaType);
        
        String response = webClient.get()
                .uri("/" + path + "/" + id + "/similar?api_key=" + apiKey + "&language=ko-KR&page=" + page)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        MediaDto result = parseMediaListResponse(response, mediaType);
        
        // 3. 받은 추천 데이터를 DB에 저장 (백그라운드 작업)
        if (result != null && result.getResults() != null && !result.getResults().isEmpty()) {
            saveRecommendationsAsync(result.getResults(), id, mediaType);
        }
        
        return result;
    }

    @Override
    public MediaDto.MediaItemDto getMediaWithCredits(Media.MediaType mediaType, Long id) {
        MediaDto.MediaItemDto media = getMediaById(mediaType, id);
        
        // DB에서 출연/제작진 정보 조회
        List<Cast> casts = castRepository.findByMediaIdAndMediaTypeOrderByOrder(id, mediaType);
        List<Crew> crews = crewRepository.findByMediaIdAndMediaType(id, mediaType);
        
        // DTO로 변환
        List<MediaDto.CastDto> castDtos = casts.stream()
                .map(mediaMapperService::mapCastToCastDto)
                .collect(Collectors.toList());
                
        List<MediaDto.CrewDto> crewDtos = crews.stream()
                .map(mediaMapperService::mapCrewToCrewDto)
                .collect(Collectors.toList());
                
        media.setCast(castDtos);
        media.setCrew(crewDtos);
        
        return media;
    }

    @Override
    public List<MediaDto.VideoDto> getMediaVideos(Media.MediaType mediaType, Long id) {
        List<Video> videos = videoRepository.findByMediaIdAndMediaType(id, mediaType);
        
        return videos.stream()
                .map(mediaMapperService::mapVideoToVideoDto)
                .collect(Collectors.toList());
    }

    @Override
    public MediaDto getAnimationsByCategory(Media.MediaCategory category, int page) {
        // 영화 애니메이션과 TV 애니메이션을 모두 가져와서 필터링
        MediaDto movieAnimations = getMediaByCategory(Media.MediaType.MOVIE, category, page);
        MediaDto tvAnimations = getMediaByCategory(Media.MediaType.TV, category, page);
        
        // 장르 ID가 16인 항목들만 필터링
        List<MediaDto.MediaItemDto> filteredMovies = filterAnimations(movieAnimations.getResults());
        List<MediaDto.MediaItemDto> filteredTvs = filterAnimations(tvAnimations.getResults());
        
        // 결합
        List<MediaDto.MediaItemDto> combinedResults = new ArrayList<>(filteredMovies);
        combinedResults.addAll(filteredTvs);
        
        // 결과 생성
        MediaDto result = new MediaDto();
        result.setResults(combinedResults);
        result.setPage(page);
        result.setTotalPages(Math.max(movieAnimations.getTotalPages(), tvAnimations.getTotalPages()));
        result.setTotalResults(combinedResults.size());
        
        return result;
    }

    @Override
    public MediaDto searchAnimations(String query, int page) {
        // 영화와 TV 모두에서 검색
        MediaDto movieResults = searchMedia(Media.MediaType.MOVIE, query, page);
        MediaDto tvResults = searchMedia(Media.MediaType.TV, query, page);
        
        // 장르 ID가 16인 항목들만 필터링
        List<MediaDto.MediaItemDto> filteredMovies = filterAnimations(movieResults.getResults());
        List<MediaDto.MediaItemDto> filteredTvs = filterAnimations(tvResults.getResults());
        
        // 결합
        List<MediaDto.MediaItemDto> combinedResults = new ArrayList<>(filteredMovies);
        combinedResults.addAll(filteredTvs);
        
        // 결과 생성
        MediaDto result = new MediaDto();
        result.setResults(combinedResults);
        result.setPage(page);
        result.setTotalPages(Math.max(movieResults.getTotalPages(), tvResults.getTotalPages()));
        result.setTotalResults(combinedResults.size());
        
        return result;
    }

    @Override
    @Transactional
    public void syncMediaData(Media.MediaType mediaType, Media.MediaCategory category) {
        // 각 미디어 타입 및 카테고리에 대한 데이터 동기화
        String path = getMediaTypePath(mediaType);
        String categoryPath = getCategoryPath(category, mediaType);
        
        for (int page = 1; page <= 5; page++) { // 최대 5페이지까지 동기화
            String response = webClient.get()
                    .uri("/" + path + "/" + categoryPath + "?api_key=" + apiKey + "&language=ko-KR&page=" + page)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                    
            if (response != null) {
                try {
                    JsonNode rootNode = objectMapper.readTree(response);
                    JsonNode resultsNode = rootNode.path("results");
                    
                    for (JsonNode mediaNode : resultsNode) {
                        MediaDto.MediaItemDto dto = mediaMapperService.mapJsonToMediaItemDto(mediaNode, mediaType);
                        dto.setCategory(category.name().toLowerCase());
                        
                        // 미디어 유형별로 저장 처리
                        saveMediaEntity(dto, mediaType, category);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to sync media data", e);
                }
            }
        }
    }
    
    // Scheduled 작업용 메서드
    @Scheduled(cron = "0 0 1 * * ?") // 매일 새벽 1시에 실행
    @Transactional
    public void scheduledMediaSync() {
        // 다양한 미디어 타입과 카테고리에 대해 동기화 실행
        syncMediaData(Media.MediaType.MOVIE, Media.MediaCategory.POPULAR);
        syncMediaData(Media.MediaType.MOVIE, Media.MediaCategory.NOW_PLAYING);
        syncMediaData(Media.MediaType.MOVIE, Media.MediaCategory.TOP_RATED);
        syncMediaData(Media.MediaType.MOVIE, Media.MediaCategory.UPCOMING);
        syncMediaData(Media.MediaType.TV, Media.MediaCategory.POPULAR);
        syncMediaData(Media.MediaType.TV, Media.MediaCategory.NOW_PLAYING);
        syncMediaData(Media.MediaType.TV, Media.MediaCategory.TOP_RATED);
    }
    
    // 애니메이션 필터링 (장르 ID가 16인 항목만 추출)
    private List<MediaDto.MediaItemDto> filterAnimations(List<MediaDto.MediaItemDto> mediaItems) {
        return mediaItems.stream()
                .filter(item -> {
                    if (item.getGenreIds() != null) {
                        return item.getGenreIds().contains(16); // 16은 애니메이션 장르 ID
                    }
                    return false;
                })
                .peek(item -> item.setMediaType("animation"))
                .collect(Collectors.toList());
    }
    
    // 미디어 유형별 경로 가져오기
    private String getMediaTypePath(Media.MediaType mediaType) {
        switch (mediaType) {
            case MOVIE:
                return "movie";
            case TV:
                return "tv";
            case ANIMATION:
                return "discover/movie"; // 애니메이션은 discover API + 장르 필터링 사용
            default:
                throw new IllegalArgumentException("Unsupported media type: " + mediaType);
        }
    }
    
    // 카테고리별 API 경로 가져오기
    private String getCategoryPath(Media.MediaCategory category, Media.MediaType mediaType) {
        if (mediaType == Media.MediaType.MOVIE) {
            switch (category) {
                case POPULAR:
                    return "popular";
                case TOP_RATED:
                    return "top_rated";
                case NOW_PLAYING:
                    return "now_playing";
                case UPCOMING:
                    return "upcoming";
                default:
                    return "popular";
            }
        } else if (mediaType == Media.MediaType.TV) {
            switch (category) {
                case POPULAR:
                    return "popular";
                case TOP_RATED:
                    return "top_rated";
                case ON_THE_AIR:
                    return "on_the_air";
                case AIRING_TODAY:
                    return "airing_today";
                default:
                    return "popular";
            }
        } else { // ANIMATION
            return "popular"; // 애니메이션은 popular 기본값 사용
        }
    }
    
    // API 응답을 MediaDto로 파싱
    private MediaDto parseMediaListResponse(String response, Media.MediaType mediaType) {
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                int page = rootNode.path("page").asInt();
                int totalPages = rootNode.path("total_pages").asInt();
                int totalResults = rootNode.path("total_results").asInt();
                JsonNode resultsNode = rootNode.path("results");
                
                List<MediaDto.MediaItemDto> mediaItems = new ArrayList<>();
                for (JsonNode mediaNode : resultsNode) {
                    MediaDto.MediaItemDto dto = mediaMapperService.mapJsonToMediaItemDto(mediaNode, mediaType);
                    mediaItems.add(dto);
                }
                
                MediaDto mediaDto = new MediaDto();
                mediaDto.setResults(mediaItems);
                mediaDto.setPage(page);
                mediaDto.setTotalPages(totalPages);
                mediaDto.setTotalResults(totalResults);
                
                return mediaDto;
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse media list response", e);
            }
        }
        
        return new MediaDto();
    }
    
    // 미디어 엔티티 저장 (영화, TV, 애니메이션에 따라 다르게 처리)
    private void saveMediaEntity(MediaDto.MediaItemDto dto, Media.MediaType mediaType, Media.MediaCategory category) {
        switch (mediaType) {
            case MOVIE:
                mediaMapperService.saveMovieEntity(dto, category);
                break;
            case TV:
                mediaMapperService.saveTvEntity(dto, category);
                break;
            case ANIMATION:
                mediaMapperService.saveAnimationEntity(dto, category);
                break;
        }
    }

    /**
     * 추천 정보를 MediaDto로 변환
     */
    private MediaDto buildMediaDtoFromRecommendations(List<MediaRecommendation> recommendations, int page) {
        // 페이징 처리
        int pageSize = 20; // TMDB API 기본 페이지 크기
        int totalSize = recommendations.size();
        int totalPages = (int) Math.ceil((double) totalSize / pageSize);
        
        // 요청된 페이지가 범위를 벗어나면 빈 결과 반환
        if (page > totalPages || page < 1) {
            MediaDto emptyResult = new MediaDto();
            emptyResult.setPage(page);
            emptyResult.setTotalPages(totalPages);
            emptyResult.setTotalResults(totalSize);
            emptyResult.setResults(new ArrayList<>());
            return emptyResult;
        }
        
        // 해당 페이지의 추천 정보 추출
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalSize);
        List<MediaRecommendation> pageRecommendations = recommendations.subList(fromIndex, toIndex);
        
        // MediaDto로 변환
        List<MediaDto.MediaItemDto> mediaItems = new ArrayList<>();
        for (MediaRecommendation recommendation : pageRecommendations) {
            // 추천된 미디어의 타입에 따라 다른 저장소에서 조회
            MediaDto.MediaItemDto itemDto = null;
            switch (recommendation.getRecommendedMediaType()) {
                case MOVIE:
                    movieRepository.findById(recommendation.getRecommendedMediaId())
                        .ifPresent(movie -> mediaItems.add(mediaMapperService.mapMovieToDto(movie)));
                    break;
                case TV:
                    tvRepository.findById(recommendation.getRecommendedMediaId())
                        .ifPresent(tv -> mediaItems.add(mediaMapperService.mapTvToDto(tv)));
                    break;
                case ANIMATION:
                    animationRepository.findById(recommendation.getRecommendedMediaId())
                        .ifPresent(animation -> mediaItems.add(mediaMapperService.mapAnimationToDto(animation)));
                    break;
            }
        }
        
        MediaDto result = new MediaDto();
        result.setResults(mediaItems);
        result.setPage(page);
        result.setTotalPages(totalPages);
        result.setTotalResults(totalSize);
        
        return result;
    }

    /**
     * 추천 정보 통계 업데이트 (접근 횟수, 마지막 접근 시간 등)
     */
    private void updateRecommendationStats(List<MediaRecommendation> recommendations) {
        for (MediaRecommendation recommendation : recommendations) {
            recommendation.incrementAccessCount();
            // 자주 접근하는 추천은 유효기간 연장
            if (recommendation.getAccessCount() % 5 == 0) {
                recommendation.extendExpiry(30);
            }
        }
        mediaRecommendationRepository.saveAll(recommendations);
    }

    /**
     * 추천 정보 비동기 저장
     */
    private void saveRecommendationsAsync(List<MediaDto.MediaItemDto> recommendedItems, Long sourceMediaId, Media.MediaType sourceType) {
        // 실제 구현에서는 Async 처리 (예: @Async 또는 ThreadPoolTaskExecutor 사용)
        new Thread(() -> {
            saveRecommendations(recommendedItems, sourceMediaId, sourceType);
        }).start();
    }

    /**
     * 추천 정보 저장
     */
    @Transactional
    public void saveRecommendations(List<MediaDto.MediaItemDto> recommendedItems, Long sourceMediaId, Media.MediaType sourceType) {
        // 각 추천 아이템을 저장
        for (MediaDto.MediaItemDto item : recommendedItems) {
            if (item.getId() == null || item.getMediaType() == null) {
                continue;
            }
            
            Media.MediaType recommendedType;
            try {
                recommendedType = Media.MediaType.valueOf(item.getMediaType().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 지원하지 않는 미디어 타입
                continue;
            }
            
            // 중복 체크
            boolean exists = mediaRecommendationRepository
                .existsByMediaIdAndRecommendedMediaIdAndMediaTypeAndRecommendedMediaType(
                    sourceMediaId, item.getId(), sourceType, recommendedType);
                
            if (!exists) {
                MediaRecommendation recommendation = new MediaRecommendation();
                recommendation.setMediaId(sourceMediaId);
                recommendation.setRecommendedMediaId(item.getId());
                recommendation.setMediaType(sourceType);
                recommendation.setRecommendedMediaType(recommendedType);
                recommendation.setSimilarityScore((float)(item.getVoteAverage() / 10.0)); // Float 타입으로 명시적 캐스팅
                
                // 동적 만료 기간 설정
                recommendation.setExpiresAt(LocalDateTime.now().plusDays(expiryDays));
                
                mediaRecommendationRepository.save(recommendation);
                
                // 해당 미디어 정보도 저장
                saveMediaEntity(item, recommendedType, Media.MediaCategory.POPULAR);
            }
        }
    }

    /**
     * 오래된 추천 정보 정리 (스케줄링된 작업)
     */
    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시에 실행
    @Transactional
    public void cleanExpiredRecommendations() {
        // 만료된 추천 정보 삭제
        mediaRecommendationRepository.deleteExpiredRecommendations(LocalDateTime.now());
        
        // 접근 빈도가 낮은 추천 정보 삭제 - 동적 기준 적용
        mediaRecommendationRepository.deleteByLowAccessCount(accessCountThreshold);
    }

    /**
     * 특정 미디어의 추천 정보 강제 갱신
     */
    @Override
    @Transactional
    public void refreshRecommendations(Media.MediaType mediaType, Long mediaId) {
        // 기존 추천 정보 삭제
        mediaRecommendationRepository.deleteByMediaIdAndMediaType(mediaId, mediaType);
        
        // TMDB API에서 새로운 추천 정보 가져오기
        String path = getMediaTypePath(mediaType);
        
        String response = webClient.get()
                .uri("/" + path + "/" + mediaId + "/similar?api_key=" + apiKey + "&language=ko-KR&page=1")
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        MediaDto result = parseMediaListResponse(response, mediaType);
        
        if (result != null && result.getResults() != null && !result.getResults().isEmpty()) {
            saveRecommendations(result.getResults(), mediaId, mediaType);
        }
    }

    /**
     * 특정 미디어의 추천 정보 삭제
     */
    @Override
    @Transactional
    public void deleteRecommendations(Media.MediaType mediaType, Long mediaId) {
        mediaRecommendationRepository.deleteByMediaIdAndMediaType(mediaId, mediaType);
    }

    /**
     * 추천 정보 통계 조회
     */
    @Override
    public Map<String, Object> getRecommendationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 총 추천 관계 수
        long totalCount = mediaRecommendationRepository.count();
        stats.put("totalRecommendations", totalCount);
        
        // 미디어 유형별 추천 관계 수
        stats.put("movieRecommendations", mediaRecommendationRepository.countByMediaType(Media.MediaType.MOVIE));
        stats.put("tvRecommendations", mediaRecommendationRepository.countByMediaType(Media.MediaType.TV));
        stats.put("animationRecommendations", mediaRecommendationRepository.countByMediaType(Media.MediaType.ANIMATION));
        
        // 접근 빈도가 낮은 추천 관계 수 (3회 미만)
        stats.put("lowAccessCount", mediaRecommendationRepository.countByLowAccessCount(3));
        
        // 만료 예정인 추천 관계 수 (7일 이내)
        LocalDateTime nextWeek = LocalDateTime.now().plusDays(7);
        stats.put("expiringNextWeek", mediaRecommendationRepository.countExpiredRecommendations(nextWeek));
        
        // 캐시 히트율 계산 (접근 횟수가 1 이상인 비율)
        long accessedCount = totalCount - mediaRecommendationRepository.countByLowAccessCount(1);
        double cacheHitRatio = totalCount > 0 ? (double) accessedCount / totalCount : 0;
        stats.put("cacheHitRatio", Math.round(cacheHitRatio * 100) / 100.0); // 소수점 둘째 자리
        
        return stats;
    }

    /**
     * 접근 빈도 기준 조정
     */
    @Override
    public void updateAccessCountThreshold(int threshold) {
        // application.properties에 저장하거나 DB 설정 테이블로 옮길것임
        // 지금은 일단 Environment를 사용하지 않고 간단하게 정적 변수로 관리
        this.accessCountThreshold = threshold;
    }

    /**
     * 만료 기간 조정
     */
    @Override
    public void updateExpiryDays(int days) {
        this.expiryDays = days;
    }
} 