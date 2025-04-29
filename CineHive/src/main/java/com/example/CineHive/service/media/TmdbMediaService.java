package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.MediaDetailsDto;
import com.example.CineHive.dto.media.MediaDto;
import com.example.CineHive.dto.media.MediaItemDto;
import com.example.CineHive.dto.media.VideoDto;
import com.example.CineHive.dto.media.GenreDto;
import com.example.CineHive.dto.media.CastDto;
import com.example.CineHive.dto.media.CrewDto;
import com.example.CineHive.dto.media.MediaCreditsDto;
import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.Animation;
import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.entity.media.Media;
import com.example.CineHive.entity.media.MediaGenre;
import com.example.CineHive.entity.media.MediaRecommendation;
import com.example.CineHive.entity.media.Movie;
import com.example.CineHive.entity.media.Tv;
import com.example.CineHive.entity.media.Video;
import com.example.CineHive.repository.media.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmdbMediaService implements MediaService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MediaMapperService mediaMapperService;
    private final VideoRepository videoRepository;
    private final CastRepository castRepository;
    private final CrewRepository crewRepository;
    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final AnimationRepository animationRepository;
    private final MediaRecommendationRepository mediaRecommendationRepository;
    private final MediaGenreRepository mediaGenreRepository;
    private final WebClient.Builder webClientBuilder;

    private int accessCountThreshold = 3;
    private int expiryDays = 30;

    @PostConstruct
    private void initWebClient() {
        this.webClient = webClientBuilder
            .baseUrl("https://api.themoviedb.org/3")
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json;charset=utf-8")
            .build();
    }

    @Override
    public MediaItemDto getMediaById(Media.MediaType mediaType, Long id) {
        // 1. 먼저 DB에서 미디어 기본 정보 조회
        MediaItemDto mediaItemDto = findMediaInDb(mediaType, id);

        // 2. DB에 없는 경우 TMDB API 호출
        if (mediaItemDto == null) {
            String path = getMediaTypePath(mediaType);

            String response = fetchFromTmdbApi("/" + path + "/" + id + "?api_key=" + apiKey + "&language=ko-KR");

            if (response != null) {
                try {
                    JsonNode rootNode = objectMapper.readTree(response);
                    MediaItemDto apiDto = mediaMapperService.mapJsonToMediaItemDto(rootNode, mediaType);

                    // API에서 받은 기본 정보를 DB에 저장
                    saveMediaEntity(apiDto, mediaType, Media.MediaCategory.DEFAULT);

                    return apiDto;
                } catch (Exception e) {
                    // API 오류인 경우 DB에 있는 정보 리턴 (부분적이더라도)
                    if (mediaItemDto != null) {
                        return mediaItemDto;
                    }
                    throw new RuntimeException("Failed to parse media details", e);
                }
            }
        }

        return mediaItemDto;
    }

    /**
     * DB에서 미디어 정보 조회
     */
    private MediaItemDto findMediaInDb(Media.MediaType mediaType, Long id) {
        return switch (mediaType) {
            case MOVIE -> movieRepository.findById(id)
                    .map(mediaMapperService::mapMovieToDto)
                    .orElse(null);
            case TV -> tvRepository.findById(id)
                    .map(mediaMapperService::mapTvToDto)
                    .orElse(null);
            case ANIMATION -> animationRepository.findById(id)
                    .map(mediaMapperService::mapAnimationToDto)
                    .orElse(null);
        };
    }

    /**
     * TMDB API에서 데이터 가져오기
     */
    private String fetchFromTmdbApi(String uri) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @Override
    public MediaDto searchMedia(Media.MediaType mediaType, String query, int page) {
        // 페이징 설정
        Pageable pageable = PageRequest.of(page - 1, 20); // 페이지는 0부터 시작, TMDB API는 1부터 시작하므로 -1

        // 미디어 타입에 따라 적절한 레포지토리에서 검색
        if (mediaType == Media.MediaType.MOVIE) {
            return searchMovies(query, page, pageable);
        } else if (mediaType == Media.MediaType.TV) {
            return searchTvs(query, page, pageable);
        } else if (mediaType == Media.MediaType.ANIMATION) {
            return searchAnimations(query, page);
        }
        
        // 지원하지 않는 미디어 타입인 경우 빈 결과 반환
        return new MediaDto();
    }

    /**
     * 영화 검색
     */
    private MediaDto searchMovies(String query, int page, Pageable pageable) {
        // 영화 레포지토리에서 검색
        Page<Movie> moviesPage = movieRepository.searchByTitleContainingIgnoreCase(query, pageable);
        List<Movie> movies = moviesPage.getContent();
        
        // 결과가 없으면 API 호출 (첫 페이지인 경우만)
        if (movies.isEmpty() && page == 1) {
            return searchMediaFromApi(Media.MediaType.MOVIE, query, page);
        }
        
        // 결과를 DTO로 변환
        List<MediaItemDto> movieDtos = movies.stream()
                .map(mediaMapperService::mapMovieToDto)
                .collect(Collectors.toList());
        
        // 결과 생성
        return createMediaDtoResult(movieDtos, page, moviesPage.getTotalPages(), (int) moviesPage.getTotalElements());
    }

    /**
     * TV 프로그램 검색
     */
    private MediaDto searchTvs(String query, int page, Pageable pageable) {
        // TV 레포지토리에서 검색
        Page<Tv> tvsPage = tvRepository.searchByTitleContainingIgnoreCase(query, pageable);
        List<Tv> tvs = tvsPage.getContent();
        
        // 결과가 없으면 API 호출 (첫 페이지인 경우만)
        if (tvs.isEmpty() && page == 1) {
            return searchMediaFromApi(Media.MediaType.TV, query, page);
        }
        
        // 결과를 DTO로 변환
        List<MediaItemDto> tvDtos = tvs.stream()
                .map(mediaMapperService::mapTvToDto)
                .collect(Collectors.toList());
        
        // 결과 생성
        return createMediaDtoResult(tvDtos, page, tvsPage.getTotalPages(), (int) tvsPage.getTotalElements());
    }

    /**
     * MediaDto 결과 생성 헬퍼 메소드
     */
    private MediaDto createMediaDtoResult(List<MediaItemDto> items, int page, int totalPages, int totalResults) {
        MediaDto result = new MediaDto();
        result.setResults(items);
        result.setPage(page);
        result.setTotalPages(totalPages);
        result.setTotalResults(totalResults);
        return result;
    }

    // API에서 미디어 검색 (기존 로직)
    private MediaDto searchMediaFromApi(Media.MediaType mediaType, String query, int page) {
        String path = getMediaTypePath(mediaType);
        
        String response = fetchFromTmdbApi("/search/" + path + "?api_key=" + apiKey + "&language=ko-KR&query=" + query + "&page=" + page);
                
        MediaDto result = parseMediaListResponse(response, mediaType);
        
        // API 결과를 DB에 저장 (백그라운드 작업으로 처리)
        if (result.getResults() != null && !result.getResults().isEmpty()) {
            saveMediaToDbAsync(result.getResults(), mediaType);
        }
        
        return result;
    }

    // API 검색 결과를 DB에 저장하는 비동기 메서드
    private void saveMediaToDbAsync(List<MediaItemDto> mediaItems, Media.MediaType mediaType) {
        CompletableFuture.runAsync(() -> {
            for (MediaItemDto mediaItem : mediaItems) {
                // 이미 존재하는지 확인
                boolean exists = switch (mediaType) {
                    case MOVIE -> movieRepository.existsById(mediaItem.getId());
                    case TV -> tvRepository.existsById(mediaItem.getId());
                    case ANIMATION -> animationRepository.existsById(mediaItem.getId());
                };
                
                if (!exists) {
                    // 각 미디어 항목의 카테고리 정보 확인 (없으면 DEFAULT로 설정)
                    Media.MediaCategory category = determineMediaCategory(mediaItem);
                    
                    // 카테고리 설정
                    mediaItem.setCategory(category.name().toLowerCase());
                    saveMediaEntity(mediaItem, mediaType, category);
                }
            }
        });
    }

    /**
     * 미디어 항목의 카테고리 결정
     */
    private Media.MediaCategory determineMediaCategory(MediaItemDto mediaItem) {
        if (mediaItem.getCategory() != null && !mediaItem.getCategory().isEmpty()) {
            try {
                return Media.MediaCategory.valueOf(mediaItem.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Media.MediaCategory.DEFAULT;
            }
        }
        return Media.MediaCategory.DEFAULT;
    }

    @Override
    public MediaDto getMediaByCategory(Media.MediaType mediaType, Media.MediaCategory category, int page, String sortBy) {
        // 로컬 DB에서 먼저 조회
        Pageable pageable = PageRequest.of(page - 1, 20);
        Page<?> mediaPage = null;
        
        // 미디어 타입별로 다른 레포지토리 사용
        if (mediaType == Media.MediaType.MOVIE) {
            mediaPage = movieRepository.findByCategory(category, pageable);
        } else if (mediaType == Media.MediaType.TV) {
            mediaPage = tvRepository.findByCategory(category, pageable);
        } else if (mediaType == Media.MediaType.ANIMATION) {
            mediaPage = animationRepository.findByCategory(category, pageable);
        }
        
        // 로컬 DB에 결과가 있는 경우
        if (mediaPage != null && !mediaPage.isEmpty()) {
            List<MediaItemDto> mediaItems = new ArrayList<>();
            
            if (mediaType == Media.MediaType.MOVIE) {
                mediaItems = ((Page<Movie>)mediaPage).getContent().stream()
                    .map(mediaMapperService::mapMovieToDto)
                    .collect(Collectors.toList());
            } else if (mediaType == Media.MediaType.TV) {
                mediaItems = ((Page<Tv>)mediaPage).getContent().stream()
                    .map(mediaMapperService::mapTvToDto)
                    .collect(Collectors.toList());
            } else if (mediaType == Media.MediaType.ANIMATION) {
                mediaItems = ((Page<Animation>)mediaPage).getContent().stream()
                    .map(mediaMapperService::mapAnimationToDto)
                    .collect(Collectors.toList());
            }
            
            MediaDto result = new MediaDto();
            result.setResults(mediaItems);
            result.setPage(page);
            result.setTotalPages(mediaPage.getTotalPages());
            result.setTotalResults((int) mediaPage.getTotalElements());
            
            // 결과가 있으면 바로 반환
            if (!result.getResults().isEmpty()) {
                return result;
            }
        }
        
        // 로컬 DB에 결과가 없는 경우에만 API 호출
        if (page == 1) {  // 첫 페이지일 때만 API 호출
            String path = getMediaTypePath(mediaType);
            String categoryPath = getCategoryPath(category, mediaType);
            
            String uriString;
            if (mediaType == Media.MediaType.ANIMATION) {
                // 애니메이션은 장르 ID 16을 사용하여 필터링 - 쿼리 파라미터 방식 수정
                uriString = "/" + path + "/" + categoryPath + "?language=ko-KR&with_genres=16";
                
                // 정렬 옵션 추가 (제공된 경우)
                if (sortBy != null && !sortBy.isEmpty()) {
                    uriString += "&sort_by=" + sortBy;
                } else {
                    // 카테고리에 따라 기본 정렬 방식 결정
                    switch (category) {
                        case POPULAR -> uriString += "&sort_by=popularity.desc";
                        case TOP_RATED -> uriString += "&sort_by=vote_average.desc";
                        case NOW_PLAYING -> uriString += "&sort_by=primary_release_date.desc";
                        case UPCOMING -> uriString += "&sort_by=primary_release_date.asc";
                        default -> uriString += "&sort_by=popularity.desc";
                    }
                }
                
                uriString += "&page=" + page;
            } else {
                // 일반 영화/TV 시리즈
                uriString = "/" + path + "/" + categoryPath + "?language=ko-KR";
                
                // 정렬 옵션 추가 (제공된 경우)
                if (sortBy != null && !sortBy.isEmpty()) {
                    uriString += "&sort_by=" + sortBy;
                }
                
                uriString += "&page=" + page;
            }
            
            try {
                System.out.println("API 호출 URL: " + uriString); // 디버깅용
                String response = webClient.get()
                        .uri(uriString)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                MediaDto result = parseMediaListResponse(response, mediaType);
                
                // API에서 받은 결과를 DB에 저장 (애니메이션 필터링 추가)
                if (result.getResults() != null && !result.getResults().isEmpty()) {
                    List<MediaItemDto> itemsToSave = result.getResults();
                    
                    // 애니메이션인 경우 장르 ID 16 필터링 추가 확인
                    if (mediaType == Media.MediaType.ANIMATION) {
                        itemsToSave = filterAnimations(itemsToSave);
                    }
                    
                    for (MediaItemDto item : itemsToSave) {
                        item.setCategory(category.name().toLowerCase());
                        saveMediaEntity(item, mediaType, category);
                    }
                }
                
                return result;
            } catch (Exception e) {
                // 오류 로깅
                System.err.println("API 호출 오류: " + e.getMessage());
                e.printStackTrace(); // 상세 오류 출력
                // 빈 결과 반환
                return new MediaDto();
            }
        }
        
        // 첫 페이지가 아니고 로컬 DB에 결과가 없는 경우 빈 결과 반환
        return new MediaDto();
        String path = getMediaTypePath(mediaType);
        String categoryPath = getCategoryPath(category, mediaType);
        
        String uriString = buildCategoryUriString(mediaType, path, categoryPath, category, page, sortBy);
        
        try {
            String response = fetchFromTmdbApi(uriString);
            return parseMediaListResponse(response, mediaType);
        } catch (Exception e) {
            // 오류 로깅
            System.err.println("API 호출 오류: " + e.getMessage());
            // 빈 결과 반환
            return new MediaDto();
        }
    }

    /**
     * 카테고리 URI 문자열 구성
     */
    private String buildCategoryUriString(Media.MediaType mediaType, String path, String categoryPath, 
                                        Media.MediaCategory category, int page, String sortBy) {
        StringBuilder uriBuilder = new StringBuilder();
        
        if (mediaType == Media.MediaType.ANIMATION) {
            // 애니메이션은 장르 ID 16을 사용하여 필터링
            uriBuilder.append("/").append(path).append("/").append(categoryPath)
                    .append("?api_key=").append(apiKey)
                    .append("&language=ko-KR&with_genres=16");
            
            // 정렬 옵션 추가 (제공된 경우)
            if (sortBy != null && !sortBy.isEmpty()) {
                uriBuilder.append("&sort_by=").append(sortBy);
            } else {
                // 카테고리에 따라 기본 정렬 방식 결정
                switch (category) {
                    case POPULAR -> uriBuilder.append("&sort_by=popularity.desc");
                    case TOP_RATED -> uriBuilder.append("&sort_by=vote_average.desc");
                    case NOW_PLAYING -> uriBuilder.append("&sort_by=primary_release_date.desc");
                    case UPCOMING -> uriBuilder.append("&sort_by=primary_release_date.asc");
                    default -> uriBuilder.append("&sort_by=popularity.desc");
                }
            }
        } else {
            // 일반 영화/TV 시리즈
            uriBuilder.append("/").append(path).append("/").append(categoryPath)
                    .append("?api_key=").append(apiKey).append("&language=ko-KR");
            
            // 정렬 옵션 추가 (제공된 경우)
            if (sortBy != null && !sortBy.isEmpty()) {
                uriBuilder.append("&sort_by=").append(sortBy);
            }
        }
        
        uriBuilder.append("&page=").append(page);
        return uriBuilder.toString();
    }
    
    @Override
    public MediaDto getMediaByCategory(Media.MediaType mediaType, Media.MediaCategory category, int page) {
        // 기본 정렬 옵션 사용
        return getMediaByCategory(mediaType, category, page, null);
    }
    
    @Override
    public MediaDto getAnimationsByCategory(Media.MediaCategory category, int page, String sortBy) {
        return getMediaByCategory(Media.MediaType.ANIMATION, category, page, sortBy);
    }
    
    @Override
    public MediaDto getAnimationsByCategory(Media.MediaCategory category, int page) {
        // 기본 정렬 옵션 사용
        return getAnimationsByCategory(category, page, null);
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
        String uriString = buildSimilarMediaUriString(mediaType, id, page);
        
        String response = fetchFromTmdbApi(uriString);
        MediaDto result = parseMediaListResponse(response, mediaType);
        
        // 3. 받은 추천 데이터를 DB에 저장 (백그라운드 작업)
        if (result != null && result.getResults() != null && !result.getResults().isEmpty()) {
            saveRecommendationsAsync(result.getResults(), id, mediaType);
        }
        
        return result;
    }

    /**
     * 유사 미디어 URI 문자열 구성
     */
    private String buildSimilarMediaUriString(Media.MediaType mediaType, Long id, int page) {
        if (mediaType == Media.MediaType.ANIMATION) {
            // 애니메이션인 경우, 해당 애니메이션 정보를 조회
            Animation animation = animationRepository.findById(id).orElse(null);
            if (animation == null) {
                // 애니메이션 정보가 없는 경우 discover API 사용하여 유사 애니메이션 가져오기 (기존 로직)
                return buildSimilarAnimationUriByGenre(id, page);
            }
            
            // 애니메이션 타입에 따라 적절한 엔드포인트 선택
            String basePath = "movie"; // 기본값은 영화
            if (animation.getAnimationType() != null && 
                "tv".equalsIgnoreCase(animation.getAnimationType().toString())) {
                basePath = "tv";
            }
            
            // similar API 사용
            return "/" + basePath + "/" + id + "/similar?api_key=" + apiKey + "&language=ko-KR&page=" + page;
        } else {
            // 일반 영화/TV 시리즈는 similar API 사용
            String path = getMediaTypePath(mediaType);
            return "/" + path + "/" + id + "/similar?api_key=" + apiKey + "&language=ko-KR&page=" + page;
        }
    }
    
    /**
     * 장르 기반 유사 애니메이션 URI 구성 (기존 로직 분리)
     */
    private String buildSimilarAnimationUriByGenre(Long id, int page) {
        // 우선 해당 애니메이션 정보 조회
        MediaItemDto mediaItem = getMediaById(Media.MediaType.ANIMATION, id);
        if (mediaItem == null || mediaItem.getGenreIds() == null || mediaItem.getGenreIds().isEmpty()) {
            return ""; // 빈 문자열 반환
        }
        
        // 장르 ID를 기반으로 유사한 콘텐츠 검색 (애니메이션 장르 16 포함)
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("/discover/movie?api_key=").append(apiKey)
                .append("&language=ko-KR&with_genres=16");
        
        // 추가 장르 포함
        for (Integer genreId : mediaItem.getGenreIds()) {
            if (genreId != 16) { // 애니메이션(16)은 이미 포함됨
                uriBuilder.append(",").append(genreId);
            }
        }
        
        uriBuilder.append("&sort_by=popularity.desc&page=").append(page);
        return uriBuilder.toString();
    }

    @Override
    public MediaCreditsDto getMediaWithCredits(Media.MediaType mediaType, Long id) {
        // DB에서 출연/제작진 정보 조회
        List<Cast> casts = castRepository.findByMediaIdAndMediaTypeOrderByOrder(id, mediaType);
        List<Crew> crews = crewRepository.findByMediaIdAndMediaType(id, mediaType);
        
        // DTO로 변환
        List<CastDto> castDtos = casts.stream()
                .map(mediaMapperService::mapCastToCastDto)
                .collect(Collectors.toList());
                
        List<CrewDto> crewDtos = crews.stream()
                .map(mediaMapperService::mapCrewToCrewDto)
                .collect(Collectors.toList());
        
        // 새로운 DTO 생성 및 반환
        return MediaCreditsDto.builder()
                .id(id)
                .cast(castDtos)
                .crew(crewDtos)
                .build();
    }

    @Override
    public List<VideoDto> getMediaVideos(Media.MediaType mediaType, Long id) {
        List<Video> videos = videoRepository.findByMediaIdAndMediaType(id, mediaType);
        
        return videos.stream()
                .map(mediaMapperService::mapVideoToVideoDto)
                .collect(Collectors.toList());
    }

    @Override
    public MediaDto searchAnimations(String query, int page) {
        // 페이징 설정
        Pageable pageable = PageRequest.of(page - 1, 20);

        // 로컬 DB에서 애니메이션 검색
        Page<Animation> animationsPage = animationRepository.searchByTitleContainingIgnoreCase(query, pageable);
        List<Animation> animations = animationsPage.getContent();
        
        // 결과가 충분하지 않으면 TMDB API 호출 고려
        if (animations.isEmpty() && page == 1) {
            // 로컬 DB에 결과가 없을 때만 API 호출 (1페이지일 경우)
            return searchAnimationsFromApi(query, page);
        }
        
        // 결과를 DTO로 변환
        List<MediaItemDto> animationDtos = animations.stream()
                .map(mediaMapperService::mapAnimationToDto)
                .collect(Collectors.toList());
        
        // 결과 생성
        return createMediaDtoResult(animationDtos, page, animationsPage.getTotalPages(), 
                (int) animationsPage.getTotalElements());
    }

    // TMDB API에서 애니메이션 검색 (리팩토링)
    private MediaDto searchAnimationsFromApi(String query, int page) {
        // 영화 애니메이션 결과 파싱
        String movieResponse = fetchFromTmdbApi("/search/movie?api_key=" + apiKey + 
                "&language=ko-KR&query=" + query + "&with_genres=16&page=" + page);
        MediaDto movieResults = parseMediaListResponse(movieResponse, Media.MediaType.ANIMATION);
        
        // TV 애니메이션 결과 파싱
        String tvResponse = fetchFromTmdbApi("/search/tv?api_key=" + apiKey + 
                "&language=ko-KR&query=" + query + "&with_genres=16&page=" + page);
        MediaDto tvResults = parseMediaListResponse(tvResponse, Media.MediaType.ANIMATION);
        
        // 결과 장르 ID가 16인 항목들만 필터링 (애니메이션만)
        List<MediaItemDto> filteredMovies = filterAnimations(movieResults.getResults());
        List<MediaItemDto> filteredTvs = filterAnimations(tvResults.getResults());
        
        // 결합
        List<MediaItemDto> combinedResults = new ArrayList<>(filteredMovies);
        combinedResults.addAll(filteredTvs);
        
        // API 결과를 DB에 저장 (백그라운드 작업으로 처리)
        saveAnimationsToDbAsync(combinedResults);
        
        // 결과 생성
        return createMediaDtoResult(combinedResults, page, 
                Math.max(movieResults.getTotalPages(), tvResults.getTotalPages()), 
                combinedResults.size());
    }

    // API 검색 결과를 DB에 저장하는 비동기 메서드
    private void saveAnimationsToDbAsync(List<MediaItemDto> animations) {
        CompletableFuture.runAsync(() -> {
            for (MediaItemDto animation : animations) {
                // 이미 존재하는지 확인
                if (!animationRepository.existsById(animation.getId())) {
                    // 각 미디어 항목의 카테고리 정보 확인 (없으면 DEFAULT로 설정)
                    Media.MediaCategory category = determineMediaCategory(animation);
                    
                    // 카테고리 설정
                    animation.setCategory(category.name().toLowerCase());
                    saveMediaEntity(animation, Media.MediaType.ANIMATION, category);
                }
            }
        });
    }

    @Override
    @Transactional
    public void syncMediaData(Media.MediaType mediaType, Media.MediaCategory category) {
        // 각 미디어 타입 및 카테고리에 대한 데이터 동기화
        String path = getMediaTypePath(mediaType);
        String categoryPath = getCategoryPath(category, mediaType);
        
        // URI 구성 - 애니메이션과 다른 미디어 타입 구분
        String uriString;
        if (mediaType == Media.MediaType.ANIMATION) {
            uriString = "/" + path + "/" + categoryPath + "?language=ko-KR&with_genres=16";
        } else {
            uriString = "/" + path + "/" + categoryPath + "?language=ko-KR";
        }
        String uriString;
        
        for (int page = 1; page <= 5; page++) { // 최대 5페이지까지 동기화
            try {
                final String pageUri = uriString + "&page=" + page;
                System.out.println("Sync API 호출: " + pageUri); // 디버깅용
                
                String response = webClient.get()
                        .uri(pageUri)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                        
                if (response != null) {
                    JsonNode rootNode = objectMapper.readTree(response);
                    JsonNode resultsNode = rootNode.path("results");
                    
                    List<MediaItemDto> dtoList = new ArrayList<>();
                    
                    for (JsonNode mediaNode : resultsNode) {
                        MediaItemDto dto = mediaMapperService.mapJsonToMediaItemDto(mediaNode, mediaType);
                        dto.setCategory(category.name().toLowerCase());
                        dtoList.add(dto);
                    }
                    
                    // 애니메이션인 경우 추가 필터링
                    if (mediaType == Media.MediaType.ANIMATION) {
                        dtoList = filterAnimations(dtoList);
                    }
                    
                    // 저장 및 상세 정보 가져오기
                    for (MediaItemDto dto : dtoList) {
                        // 미디어 유형별로 저장 처리
                        saveMediaEntity(dto, mediaType, category);
                        
                        // 각 항목별로 상세 정보 추가 조회 (출연진, 제작진, 비디오 포함)
                        fetchAndSaveMediaDetails(dto.getId(), mediaType);
                    }
                }
            } catch (Exception e) {
                System.err.println("동기화 오류 (페이지 " + page + "): " + e.getMessage());
                e.printStackTrace();
        // 애니메이션은 discover API 사용하는 특별한 처리
        if (mediaType == Media.MediaType.ANIMATION) {
            for (int page = 1; page <= 5; page++) { // 최대 5페이지까지 동기화
                uriString = "/discover/movie?api_key=" + apiKey + 
                            "&language=ko-KR&with_genres=16" + // 16은 애니메이션 장르 ID
                            "&sort_by=";
                            
                // 카테고리별 정렬 방식 결정
                switch(category) {
                    case POPULAR -> uriString += "popularity.desc";
                    case TOP_RATED -> uriString += "vote_average.desc";
                    case NOW_PLAYING -> uriString += "primary_release_date.desc";
                    case UPCOMING -> uriString += "primary_release_date.asc";
                    default -> uriString += "popularity.desc";
                }
                
                uriString += "&page=" + page;
                
                // API 호출 및 데이터 처리
                processApiResponse(uriString, mediaType, category);
            }
        } else {
            // 일반 영화/TV 시리즈에 대한 기존 처리
            for (int page = 1; page <= 5; page++) { // 최대 5페이지까지 동기화
                uriString = "/" + path + "/" + categoryPath + 
                        "?api_key=" + apiKey + "&language=ko-KR&page=" + page;
                        
                // API 호출 및 데이터 처리
                processApiResponse(uriString, mediaType, category);
            }
        }
    }
    
    /**
     * API 응답을 처리하여 데이터베이스에 저장
     */
    private void processApiResponse(String uriString, Media.MediaType mediaType, Media.MediaCategory category) {
        String response = fetchFromTmdbApi(uriString);
                
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode resultsNode = rootNode.path("results");
                
                for (JsonNode mediaNode : resultsNode) {
                    MediaItemDto dto = mediaMapperService.mapJsonToMediaItemDto(mediaNode, mediaType);
                    dto.setCategory(category.name().toLowerCase());
                    
                    // 미디어 유형별로 저장 처리
                    saveMediaEntity(dto, mediaType, category);
                    
                    // 각 항목별로 상세 정보 추가 조회 (출연진, 제작진, 비디오 포함)
                    fetchAndSaveMediaDetails(dto.getId(), mediaType);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to sync media data", e);
            }
        }
    }
    
    /**
     * 미디어 상세 정보 조회 및 저장 (출연진, 제작진, 비디오 등)
     */
    private void fetchAndSaveMediaDetails(Long id, Media.MediaType mediaType) {
        String path = getMediaTypePath(mediaType);
        
        try {
            String uriString = "/" + path + "/" + id + "?language=ko-KR&append_to_response=videos,credits";
            System.out.println("상세 정보 API 호출: " + uriString); // 디버깅용
            
            String response = webClient.get()
                    .uri(uriString)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                    
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                
                // 기본 정보 저장
                MediaItemDto basicDto = mediaMapperService.mapJsonToMediaItemDto(rootNode, mediaType);
                
                // 애니메이션인 경우, 장르 ID 확인
                if (mediaType == Media.MediaType.ANIMATION) {
                    List<Integer> genreIds = basicDto.getGenreIds();
                    if (genreIds == null || !genreIds.contains(16)) {
                        // 애니메이션 장르 ID가 없으면 스킵
                        System.out.println("ID " + id + "는 애니메이션이 아닙니다. 장르 IDs: " + genreIds);
                        return;
                    }
                }
                
                saveMediaEntity(basicDto, mediaType, Media.MediaCategory.DEFAULT);
                
                // 기존 엔티티의 카테고리 유지
                Media.MediaCategory existingCategory = getExistingCategory(id, mediaType);
                
                // 카테고리 정보 유지하면서 저장
                saveMediaEntity(basicDto, mediaType, existingCategory);
                
                // 비디오 정보 추출 및 저장
                List<VideoDto> videoDtos = mediaMapperService.extractVideosFromJson(rootNode);
                if (!videoDtos.isEmpty()) {
                    mediaMapperService.saveVideos(videoDtos, id, mediaType);
                }
                
                // 출연진 정보 추출 및 저장
                List<CastDto> castDtos = mediaMapperService.extractCastFromJson(rootNode);
                if (!castDtos.isEmpty()) {
                    mediaMapperService.saveCast(castDtos, id, mediaType);
                }
                
                // 제작진 정보 추출 및 저장
                List<CrewDto> crewDtos = mediaMapperService.extractCrewFromJson(rootNode);
                if (!crewDtos.isEmpty()) {
                    mediaMapperService.saveCrew(crewDtos, id, mediaType);
                }
            }
        } catch (Exception e) {
            // 오류가 발생해도 동기화 프로세스는 계속 진행 (로그만 기록)
            System.err.println("상세 정보 가져오기 실패 - " + mediaType + " ID: " + id);
            System.err.println("오류 메시지: " + e.getMessage());
        }
    }
    
    /**
     * 기존 미디어의 카테고리 조회
     */
    private Media.MediaCategory getExistingCategory(Long id, Media.MediaType mediaType) {
        return switch (mediaType) {
            case MOVIE -> movieRepository.findById(id)
                    .map(Movie::getCategory)
                    .orElse(Media.MediaCategory.DEFAULT);
            case TV -> tvRepository.findById(id)
                    .map(Tv::getCategory)
                    .orElse(Media.MediaCategory.DEFAULT);
            case ANIMATION -> animationRepository.findById(id)
                    .map(Animation::getCategory)
                    .orElse(Media.MediaCategory.DEFAULT);
        };
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
        
        // 애니메이션 데이터 동기화
        syncMediaData(Media.MediaType.ANIMATION, Media.MediaCategory.POPULAR);
        syncMediaData(Media.MediaType.ANIMATION, Media.MediaCategory.TOP_RATED);
        syncMediaData(Media.MediaType.ANIMATION, Media.MediaCategory.NOW_PLAYING);
        syncMediaData(Media.MediaType.ANIMATION, Media.MediaCategory.UPCOMING);
    }
    
    // 애니메이션 필터링 (장르 ID가 16인 항목만 추출)
    private List<MediaItemDto> filterAnimations(List<MediaItemDto> mediaItems) {
        if (mediaItems == null) {
            return new ArrayList<>();
        }
        
        return mediaItems.stream()
                .filter(item -> {
                    if (item.getGenreIds() != null) {
                        return item.getGenreIds().contains(16); // 16은 애니메이션 장르 ID
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
    
    // 미디어 유형별 경로 가져오기
    private String getMediaTypePath(Media.MediaType mediaType) {
        return switch (mediaType) {
            case MOVIE -> "movie";
            case TV -> "tv";
            case ANIMATION -> "movie"; // 애니메이션도 movie 엔드포인트 사용
            case ANIMATION -> "discover"; // 애니메이션은 discover API 사용
        };
    }
    
    // 카테고리별 API 경로 가져오기
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
        } else { // ANIMATION - 기본 경로만 반환하고 파라미터는 호출 시 추가
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
    
    // API 응답을 MediaDto로 파싱
    private MediaDto parseMediaListResponse(String response, Media.MediaType mediaType) {
        if (response == null) {
            return new MediaDto();
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            int page = rootNode.path("page").asInt();
            int totalPages = rootNode.path("total_pages").asInt();
            int totalResults = rootNode.path("total_results").asInt();
            JsonNode resultsNode = rootNode.path("results");
            
            List<MediaItemDto> mediaItems = new ArrayList<>();
            for (JsonNode mediaNode : resultsNode) {
                MediaItemDto dto = mediaMapperService.mapJsonToMediaItemDto(mediaNode, mediaType);
                mediaItems.add(dto);
            }
            
            return createMediaDtoResult(mediaItems, page, totalPages, totalResults);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse media list response", e);
        }
    }
    
    // 미디어 엔티티 저장 (영화, TV, 애니메이션에 따라 다르게 처리)
    private void saveMediaEntity(MediaItemDto dto, Media.MediaType mediaType, Media.MediaCategory category) {
        switch (mediaType) {
            case MOVIE -> mediaMapperService.saveMovieEntity(dto, category);
            case TV -> mediaMapperService.saveTvEntity(dto, category);
            case ANIMATION -> mediaMapperService.saveAnimationEntity(dto, category);
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
            return createMediaDtoResult(new ArrayList<>(), page, totalPages, totalSize);
        }
        
        // 해당 페이지의 추천 정보 추출
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalSize);
        List<MediaRecommendation> pageRecommendations = recommendations.subList(fromIndex, toIndex);
        
        // MediaDto로 변환
        List<MediaItemDto> mediaItems = pageRecommendations.stream()
                .map(this::mapRecommendationToMediaItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        return createMediaDtoResult(mediaItems, page, totalPages, totalSize);
    }

    /**
     * 추천 정보를 MediaItemDto로 변환
     */
    private MediaItemDto mapRecommendationToMediaItem(MediaRecommendation recommendation) {
        return switch (recommendation.getRecommendedMediaType()) {
            case MOVIE -> movieRepository.findById(recommendation.getRecommendedMediaId())
                    .map(mediaMapperService::mapMovieToDto)
                    .orElse(null);
            case TV -> tvRepository.findById(recommendation.getRecommendedMediaId())
                    .map(mediaMapperService::mapTvToDto)
                    .orElse(null);
            case ANIMATION -> animationRepository.findById(recommendation.getRecommendedMediaId())
                    .map(mediaMapperService::mapAnimationToDto)
                    .orElse(null);
        };
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
    private void saveRecommendationsAsync(List<MediaItemDto> recommendedItems, Long sourceMediaId, Media.MediaType sourceType) {
        CompletableFuture.runAsync(() -> {
            saveRecommendations(recommendedItems, sourceMediaId, sourceType);
        });
    }

    /**
     * 추천 정보 저장
     */
    @Transactional
    public void saveRecommendations(List<MediaItemDto> recommendedItems, Long sourceMediaId, Media.MediaType sourceType) {
        // 각 추천 아이템을 저장
        for (MediaItemDto item : recommendedItems) {
            if (item.getId() == null || item.getMediaType() == null) {
                continue;
            }
            
            Media.MediaType recommendedType = parseMediaType(item.getMediaType());
            if (recommendedType == null) {
                continue; // 지원하지 않는 미디어 타입
            }
            
            // 중복 체크
            boolean exists = mediaRecommendationRepository
                .existsByMediaIdAndRecommendedMediaIdAndMediaTypeAndRecommendedMediaType(
                    sourceMediaId, item.getId(), sourceType, recommendedType);
                
            if (!exists) {
                saveRecommendation(item, sourceMediaId, sourceType, recommendedType);
            }
        }
    }

    /**
     * 단일 추천 정보 저장
     */
    private void saveRecommendation(MediaItemDto item, Long sourceMediaId, Media.MediaType sourceType, Media.MediaType recommendedType) {
        MediaRecommendation recommendation = new MediaRecommendation();
        recommendation.setMediaId(sourceMediaId);
        recommendation.setRecommendedMediaId(item.getId());
        recommendation.setMediaType(sourceType);
        recommendation.setRecommendedMediaType(recommendedType);
        recommendation.setSimilarityScore((float)(item.getVoteAverage() / 10.0));
        
        // 동적 만료 기간 설정
        recommendation.setExpiresAt(LocalDateTime.now().plusDays(expiryDays));
        
        mediaRecommendationRepository.save(recommendation);
        
        // 해당 미디어 정보도 저장
        saveMediaEntity(item, recommendedType, Media.MediaCategory.POPULAR);
    }

    /**
     * 문자열을 MediaType으로 변환
     */
    private Media.MediaType parseMediaType(String mediaTypeStr) {
        try {
            return Media.MediaType.valueOf(mediaTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
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
        
        String response = fetchFromTmdbApi("/" + path + "/" + mediaId + "/similar?api_key=" + apiKey + "&language=ko-KR&page=1");
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
        this.accessCountThreshold = threshold;
    }

    /**
     * 만료 기간 조정
     */
    @Override
    public void updateExpiryDays(int days) {
        this.expiryDays = days;
    }

    @Override
    public MediaDetailsDto getMediaDetails(Media.MediaType mediaType, Long id) {
        MediaDetailsDto detailsDto = new MediaDetailsDto();
        
        // 기본 미디어 정보 조회
        MediaItemDto mediaItemDto = getMediaById(mediaType, id);
        if (mediaItemDto == null) {
            return null;
        }
        
        detailsDto.setMedia(mediaItemDto);
        
        // 장르 정보 추가
        addGenreInfoToMedia(mediaItemDto, mediaType, id);
        
        // 출연진 정보 조회
        List<CastDto> castDtos = castRepository.findByMediaIdAndMediaTypeOrderByOrder(id, mediaType)
                .stream()
                .map(mediaMapperService::mapCastToCastDto)
                .collect(Collectors.toList());
        detailsDto.setCredits(castDtos);
        
        // 제작진 정보 조회
        List<CrewDto> crewDtos = crewRepository.findByMediaIdAndMediaType(id, mediaType)
                .stream()
                .map(mediaMapperService::mapCrewToCrewDto)
                .collect(Collectors.toList());
        detailsDto.setCrew(crewDtos);
        
        // 비디오 정보 조회
        List<VideoDto> videoDtos = videoRepository.findByMediaIdAndMediaType(id, mediaType)
                .stream()
                .map(mediaMapperService::mapVideoToVideoDto)
                .collect(Collectors.toList());
        detailsDto.setVideos(videoDtos);
        
        // DB에 출연진, 제작진, 비디오 정보가 없는 경우 API에서 상세 정보 조회
        if (castDtos.isEmpty() || crewDtos.isEmpty() || videoDtos.isEmpty()) {
            fetchAndSaveMediaDetails(id, mediaType);
            
            // 다시 DB에서 정보 조회
            detailsDto = refreshDetailsFromDb(id, mediaType, detailsDto);
        }
        
        // 유사 미디어 조회
        MediaDto similarMedia = getSimilarMedia(mediaType, id, 1);
        if (similarMedia != null && similarMedia.getResults() != null) {
            detailsDto.setSimilar(similarMedia.getResults());
        }
        
        return detailsDto;
    }

    /**
     * DB에서 미디어 상세 정보 새로고침
     */
    private MediaDetailsDto refreshDetailsFromDb(Long id, Media.MediaType mediaType, MediaDetailsDto detailsDto) {
        // 출연진 정보 새로고침
        List<CastDto> castDtos = castRepository.findByMediaIdAndMediaTypeOrderByOrder(id, mediaType)
                .stream()
                .map(mediaMapperService::mapCastToCastDto)
                .collect(Collectors.toList());
        detailsDto.setCredits(castDtos);
        
        // 제작진 정보 새로고침
        List<CrewDto> crewDtos = crewRepository.findByMediaIdAndMediaType(id, mediaType)
                .stream()
                .map(mediaMapperService::mapCrewToCrewDto)
                .collect(Collectors.toList());
        detailsDto.setCrew(crewDtos);
        
        // 비디오 정보 새로고침
        List<VideoDto> videoDtos = videoRepository.findByMediaIdAndMediaType(id, mediaType)
                .stream()
                .map(mediaMapperService::mapVideoToVideoDto)
                .collect(Collectors.toList());
        detailsDto.setVideos(videoDtos);
        
        return detailsDto;
    }
    
    /**
     * 미디어 항목에 장르 정보를 추가
     */
    private void addGenreInfoToMedia(MediaItemDto mediaItemDto, Media.MediaType mediaType, Long mediaId) {
        if (mediaItemDto == null) return;
        
        // 장르 정보가 이미 있으면 추가하지 않음
        if (mediaItemDto.getGenres() != null && !mediaItemDto.getGenres().isEmpty()) {
            return;
        }
        
        List<MediaGenre> mediaGenres = mediaGenreRepository.findByMediaIdAndMediaType(mediaId, mediaType);
        
        List<GenreDto> genreDtos = mediaGenres.stream()
                .map(MediaGenre::getGenre)
                .filter(Objects::nonNull)
                .map(this::mapGenreToDto)
                .collect(Collectors.toList());
        
        mediaItemDto.setGenres(genreDtos);
    }

    /**
     * Genre 엔티티를 GenreDto로 변환
     */
    private GenreDto mapGenreToDto(Genre genre) {
        GenreDto genreDto = new GenreDto();
        genreDto.setId(genre.getId());
        genreDto.setName(genre.getName());
        return genreDto;
    }
} 