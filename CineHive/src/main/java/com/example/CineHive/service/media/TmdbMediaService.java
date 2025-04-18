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
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private MediaGenreRepository mediaGenreRepository;

    private int accessCountThreshold = 3;
    private int expiryDays = 30;

    public TmdbMediaService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    @Override
    public MediaItemDto getMediaById(Media.MediaType mediaType, Long id) {
        // 1. 먼저 DB에서 미디어 기본 정보 조회
        MediaItemDto mediaItemDto = null;

        switch (mediaType) {
            case MOVIE -> {
                Optional<Movie> movieOpt = movieRepository.findById(id);
                if (movieOpt.isPresent()) {
                    mediaItemDto = mediaMapperService.mapMovieToDto(movieOpt.get());

                    // 장르 정보 추가
                    addGenreInfoToMedia(mediaItemDto, mediaType, id);

                    // 출연진 정보 추가
                    List<CastDto> castDtos = castRepository.findByMediaIdAndMediaTypeOrderByOrder(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapCastToCastDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setCast(castDtos);

                    // 제작진 정보 추가
                    List<CrewDto> crewDtos = crewRepository.findByMediaIdAndMediaType(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapCrewToCrewDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setCrew(crewDtos);

                    // 비디오 정보 추가
                    List<VideoDto> videoDtos = videoRepository.findByMediaIdAndMediaType(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapVideoToVideoDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setVideos(videoDtos);
                }
            }
            case TV -> {
                Optional<Tv> tvOpt = tvRepository.findById(id);
                if (tvOpt.isPresent()) {
                    mediaItemDto = mediaMapperService.mapTvToDto(tvOpt.get());

                    // 장르, 출연진, 제작진, 비디오 정보 추가 (위와 동일한 패턴)
                    addGenreInfoToMedia(mediaItemDto, mediaType, id);

                    List<CastDto> castDtos = castRepository.findByMediaIdAndMediaTypeOrderByOrder(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapCastToCastDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setCast(castDtos);

                    List<CrewDto> crewDtos = crewRepository.findByMediaIdAndMediaType(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapCrewToCrewDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setCrew(crewDtos);

                    List<VideoDto> videoDtos = videoRepository.findByMediaIdAndMediaType(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapVideoToVideoDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setVideos(videoDtos);
                }
            }
            case ANIMATION -> {
                Optional<Animation> animationOpt = animationRepository.findById(id);
                if (animationOpt.isPresent()) {
                    mediaItemDto = mediaMapperService.mapAnimationToDto(animationOpt.get());

                    // 장르, 출연진, 제작진, 비디오 정보 추가 (위와 동일한 패턴)
                    addGenreInfoToMedia(mediaItemDto, mediaType, id);

                    List<CastDto> castDtos = castRepository.findByMediaIdAndMediaTypeOrderByOrder(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapCastToCastDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setCast(castDtos);

                    List<CrewDto> crewDtos = crewRepository.findByMediaIdAndMediaType(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapCrewToCrewDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setCrew(crewDtos);

                    List<VideoDto> videoDtos = videoRepository.findByMediaIdAndMediaType(id, mediaType)
                            .stream()
                            .map(mediaMapperService::mapVideoToVideoDto)
                            .collect(Collectors.toList());
                    mediaItemDto.setVideos(videoDtos);
                }
            }
        }

        // 2. DB에 없거나 상세 정보가 누락된 경우 TMDB API 호출
        if (mediaItemDto == null || isDetailsMissing(mediaItemDto)) {
            String path = getMediaTypePath(mediaType);

            String response = webClient.get()
                    .uri("/" + path + "/" + id + "?api_key=" + apiKey + "&language=ko-KR&append_to_response=videos,credits")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                try {
                    JsonNode rootNode = objectMapper.readTree(response);
                    MediaItemDto apiDto = mediaMapperService.mapJsonToMediaItemDto(rootNode, mediaType);

                    // API에서 받은 정보를 DB에 저장
                    saveCompleteMediaData(apiDto, mediaType);

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

    // 상세 정보 누락 여부 확인
    private boolean isDetailsMissing(MediaItemDto dto) {
        return (dto.getVideos() == null || dto.getVideos().isEmpty()) ||
                (dto.getCast() == null || dto.getCast().isEmpty()) ||
                (dto.getCrew() == null || dto.getCrew().isEmpty()) ||
                (dto.getGenres() == null || dto.getGenres().isEmpty());
    }

    // 완전한 미디어 데이터 저장 (기본 + 출연진 + 제작진 + 비디오)
    @Transactional
    protected void saveCompleteMediaData(MediaItemDto dto, Media.MediaType mediaType) {
        Media.MediaCategory category = Media.MediaCategory.DEFAULT;
        if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
            try {
                category = Media.MediaCategory.valueOf(dto.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 기본값 유지
            }
        }

        saveMediaEntity(dto, mediaType, category);
    }

    @Override
    public MediaDto searchMedia(Media.MediaType mediaType, String query, int page) {
        // 페이징 설정
        Pageable pageable = PageRequest.of(page - 1, 20); // 페이지는 0부터 시작, TMDB API는 1부터 시작하므로 -1

        // 미디어 타입에 따라 적절한 레포지토리에서 검색
        if (mediaType == Media.MediaType.MOVIE) {
            // 영화인 경우 영화 레포지토리에서 검색
            Page<Movie> moviesPage = movieRepository.searchByTitleContainingIgnoreCase(query, pageable);
            List<Movie> movies = moviesPage.getContent();
            
            // 결과가 없으면 API 호출 (첫 페이지인 경우만)
            if (movies.isEmpty() && page == 1) {
                return searchMediaFromApi(mediaType, query, page);
            }
            
            // 결과를 DTO로 변환
            List<MediaItemDto> movieDtos = movies.stream()
                    .map(mediaMapperService::mapMovieToDto)
                    .collect(Collectors.toList());
            
            // 결과 생성
            MediaDto result = new MediaDto();
            result.setResults(movieDtos);
            result.setPage(page);
            result.setTotalPages(moviesPage.getTotalPages());
            result.setTotalResults((int) moviesPage.getTotalElements());
            
            if (!result.getResults().isEmpty()) {
                return result;
            }
        } else if (mediaType == Media.MediaType.TV) {
            // TV인 경우 TV 레포지토리에서 검색
            Page<Tv> tvsPage = tvRepository.searchByTitleContainingIgnoreCase(query, pageable);
            List<Tv> tvs = tvsPage.getContent();
            
            // 결과가 없으면 API 호출 (첫 페이지인 경우만)
            if (tvs.isEmpty() && page == 1) {
                return searchMediaFromApi(mediaType, query, page);
            }
            
            // 결과를 DTO로 변환
            List<MediaItemDto> tvDtos = tvs.stream()
                    .map(mediaMapperService::mapTvToDto)
                    .collect(Collectors.toList());
            
            // 결과 생성
            MediaDto result = new MediaDto();
            result.setResults(tvDtos);
            result.setPage(page);
            result.setTotalPages(tvsPage.getTotalPages());
            result.setTotalResults((int) tvsPage.getTotalElements());
            
            if (!result.getResults().isEmpty()) {
                return result;
            }
        } else if (mediaType == Media.MediaType.ANIMATION) {
            // 애니메이션인 경우 기존 searchAnimations 메서드 호출
            return searchAnimations(query, page);
        }
        
        // 지원하지 않는 미디어 타입인 경우 빈 결과 반환
        return new MediaDto();
    }

    // API에서 미디어 검색 (기존 로직)
    private MediaDto searchMediaFromApi(Media.MediaType mediaType, String query, int page) {
        String path = getMediaTypePath(mediaType);
        
        String response = webClient.get()
                .uri("/search/" + path + "?api_key=" + apiKey + "&language=ko-KR&query=" + query + "&page=" + page)
                .retrieve()
                .bodyToMono(String.class)
                .block();
                
        MediaDto result = parseMediaListResponse(response, mediaType);
        
        // API 결과를 DB에 저장 (백그라운드 작업으로 처리)
        if (result.getResults() != null && !result.getResults().isEmpty()) {
            saveMediaToDbAsync(result.getResults(), mediaType);
        }
        
        return result;
    }

    // API 검색 결과를 DB에 저장하는 비동기 메서드
    private void saveMediaToDbAsync(List<MediaItemDto> mediaItems, Media.MediaType mediaType) {
        new Thread(() -> {
            for (MediaItemDto mediaItem : mediaItems) {
                // 이미 존재하는지 확인
                boolean exists = switch (mediaType) {
                    case MOVIE -> movieRepository.existsById(mediaItem.getId());
                    case TV -> tvRepository.existsById(mediaItem.getId());
                    case ANIMATION -> animationRepository.existsById(mediaItem.getId());
                };
                
                if (!exists) {
                    mediaItem.setCategory(Media.MediaCategory.DEFAULT.name().toLowerCase());
                    saveMediaEntity(mediaItem, mediaType, Media.MediaCategory.DEFAULT);
                }
            }
        }).start();
    }

    @Override
    public MediaDto getMediaByCategory(Media.MediaType mediaType, Media.MediaCategory category, int page, String sortBy) {
        String path = getMediaTypePath(mediaType);
        String categoryPath = getCategoryPath(category, mediaType);
        
        String uriString;
        if (mediaType == Media.MediaType.ANIMATION) {
            // 애니메이션은 장르 ID 16을 사용하여 필터링
            uriString = "/" + path + "/" + categoryPath + "?api_key=" + apiKey 
                + "&language=ko-KR&with_genres=16";
            
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
            uriString = "/" + path + "/" + categoryPath + "?api_key=" + apiKey + "&language=ko-KR";
            
            // 정렬 옵션 추가 (제공된 경우)
            if (sortBy != null && !sortBy.isEmpty()) {
                uriString += "&sort_by=" + sortBy;
            }
            
            uriString += "&page=" + page;
        }
        
        try {
            String response = webClient.get()
                    .uri(uriString)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                    
            return parseMediaListResponse(response, mediaType);
        } catch (Exception e) {
            // 오류 로깅
            System.err.println("API 호출 오류: " + e.getMessage());
            // 빈 결과 반환
            return new MediaDto();
        }
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
        String path = getMediaTypePath(mediaType);
        String uriString;
        
        if (mediaType == Media.MediaType.ANIMATION) {
            // 애니메이션인 경우 discover API 사용하여 유사 애니메이션 가져오기
            // 우선 해당 애니메이션 정보 조회
            MediaItemDto mediaItem = getMediaById(mediaType, id);
            if (mediaItem == null || mediaItem.getGenreIds() == null || mediaItem.getGenreIds().isEmpty()) {
                return new MediaDto(); // 빈 결과 반환
            }
            
            // 장르 ID를 기반으로 유사한 콘텐츠 검색 (애니메이션 장르 16 포함)
            uriString = "/discover/movie?api_key=" + apiKey + "&language=ko-KR&with_genres=16";
            
            // 추가 장르 포함
            for (Integer genreId : mediaItem.getGenreIds()) {
                if (genreId != 16) { // 애니메이션(16)은 이미 포함됨
                    uriString += "," + genreId;
                }
            }
            
            uriString += "&sort_by=popularity.desc&page=" + page;
        } else {
            // 일반 영화/TV 시리즈는 similar API 사용
            uriString = "/" + path + "/" + id + "/similar?api_key=" + apiKey + "&language=ko-KR&page=" + page;
        }
        
        String response = webClient.get()
                .uri(uriString)
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
        // 1. 로컬 DB에서 애니메이션 검색
        // 페이징 설정
        Pageable pageable = PageRequest.of(page - 1, 20); // 페이지는 0부터 시작, TMDB API는 1부터 시작하므로 -1

        // 로컬 DB에서 제목으로 검색
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
        MediaDto result = new MediaDto();
        result.setResults(animationDtos);
        result.setPage(page);
        result.setTotalPages(animationsPage.getTotalPages());
        result.setTotalResults((int) animationsPage.getTotalElements());
        
        if (!result.getResults().isEmpty()) {
            return result;
        }
        
        return result; // 비어있더라도 결과 반환
    }

    // TMDB API에서 애니메이션 검색 (기존 로직)
    private MediaDto searchAnimationsFromApi(String query, int page) {
        // TMDB API에서 검색 결과 가져오기
        String response = webClient.get()
                .uri("/search/movie?api_key=" + apiKey + "&language=ko-KR&query=" + query + "&with_genres=16&page=" + page)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        // 영화 애니메이션 결과 파싱
        MediaDto movieResults = parseMediaListResponse(response, Media.MediaType.ANIMATION);
        
        // TV 애니메이션도 검색
        response = webClient.get()
                .uri("/search/tv?api_key=" + apiKey + "&language=ko-KR&query=" + query + "&with_genres=16&page=" + page)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        // TV 애니메이션 결과 파싱
        MediaDto tvResults = parseMediaListResponse(response, Media.MediaType.ANIMATION);
        
        // 결과 장르 ID가 16인 항목들만 필터링 (애니메이션만)
        List<MediaItemDto> filteredMovies = filterAnimations(movieResults.getResults());
        List<MediaItemDto> filteredTvs = filterAnimations(tvResults.getResults());
        
        // 결합
        List<MediaItemDto> combinedResults = new ArrayList<>(filteredMovies);
        combinedResults.addAll(filteredTvs);
        
        // API 결과를 DB에 저장 (백그라운드 작업으로 처리)
        saveAnimationsToDbAsync(combinedResults);
        
        // 결과 생성
        MediaDto result = new MediaDto();
        result.setResults(combinedResults);
        result.setPage(page);
        result.setTotalPages(Math.max(movieResults.getTotalPages(), tvResults.getTotalPages()));
        result.setTotalResults(combinedResults.size());
        
        if (!result.getResults().isEmpty()) {
            return result;
        }
        
        return result; // 비어있더라도 결과 반환
    }

    // API 검색 결과를 DB에 저장하는 비동기 메서드
    private void saveAnimationsToDbAsync(List<MediaItemDto> animations) {
        new Thread(() -> {
            for (MediaItemDto animation : animations) {
                // 이미 존재하는지 확인
                if (!animationRepository.existsById(animation.getId())) {
                    animation.setCategory(Media.MediaCategory.DEFAULT.name().toLowerCase());
                    saveMediaEntity(animation, Media.MediaType.ANIMATION, Media.MediaCategory.DEFAULT);
                }
            }
        }).start();
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
    }
    
    /**
     * 미디어 상세 정보 조회 및 저장 (출연진, 제작진, 비디오 등)
     */
    private void fetchAndSaveMediaDetails(Long id, Media.MediaType mediaType) {
        String path = getMediaTypePath(mediaType);
        
        try {
            String response = webClient.get()
                    .uri("/" + path + "/" + id + "?api_key=" + apiKey + "&language=ko-KR&append_to_response=videos,credits")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                    
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                MediaItemDto detailsDto = mediaMapperService.mapJsonToMediaItemDto(rootNode, mediaType);
                
                // 비디오 정보 저장
                if (detailsDto.getVideos() != null && !detailsDto.getVideos().isEmpty()) {
                    mediaMapperService.saveVideos(detailsDto.getVideos(), id, mediaType);
                }
                
                // 출연진 정보 저장
                if (detailsDto.getCast() != null && !detailsDto.getCast().isEmpty()) {
                    mediaMapperService.saveCast(detailsDto.getCast(), id, mediaType);
                }
                
                // 제작진 정보 저장
                if (detailsDto.getCrew() != null && !detailsDto.getCrew().isEmpty()) {
                    mediaMapperService.saveCrew(detailsDto.getCrew(), id, mediaType);
                }
            }
        } catch (Exception e) {
            // 오류가 발생해도 동기화 프로세스는 계속 진행 (로그만 기록)
            System.err.println("Failed to fetch details for " + mediaType + " with ID: " + id + " - " + e.getMessage());
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
        
        // 애니메이션 데이터 동기화 추가
        syncMediaData(Media.MediaType.ANIMATION, Media.MediaCategory.POPULAR);
        syncMediaData(Media.MediaType.ANIMATION, Media.MediaCategory.TOP_RATED);
    }
    
    // 애니메이션 필터링 (장르 ID가 16인 항목만 추출)
    private List<MediaItemDto> filterAnimations(List<MediaItemDto> mediaItems) {
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
            case ANIMATION -> "discover/movie"; // 애니메이션은 discover API 사용
        };
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
            return "movie"; // 애니메이션은 discover/movie 엔드포인트 사용
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
                
                List<MediaItemDto> mediaItems = new ArrayList<>();
                for (JsonNode mediaNode : resultsNode) {
                    MediaItemDto dto = mediaMapperService.mapJsonToMediaItemDto(mediaNode, mediaType);
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
        List<MediaItemDto> mediaItems = new ArrayList<>();
        for (MediaRecommendation recommendation : pageRecommendations) {
            // 추천된 미디어의 타입에 따라 다른 저장소에서 조회
            MediaItemDto itemDto = switch (recommendation.getRecommendedMediaType()) {
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
            
            if (itemDto != null) {
                mediaItems.add(itemDto);
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
    private void saveRecommendationsAsync(List<MediaItemDto> recommendedItems, Long sourceMediaId, Media.MediaType sourceType) {
        // 실제 구현에서는 Async 처리 (예: @Async 또는 ThreadPoolTaskExecutor 사용)
        new Thread(() -> {
            saveRecommendations(recommendedItems, sourceMediaId, sourceType);
        }).start();
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
                .map(cast -> mediaMapperService.mapCastToCastDto(cast))
                .collect(Collectors.toList());
        detailsDto.setCredits(castDtos);
        
        // 제작진 정보 조회
        List<CrewDto> crewDtos = crewRepository.findByMediaIdAndMediaType(id, mediaType)
                .stream()
                .map(crew -> mediaMapperService.mapCrewToCrewDto(crew))
                .collect(Collectors.toList());
        detailsDto.setCrew(crewDtos);
        
        // 비디오 정보 조회
        List<VideoDto> videoDtos = videoRepository.findByMediaIdAndMediaType(id, mediaType)
                .stream()
                .map(video -> mediaMapperService.mapVideoToVideoDto(video))
                .collect(Collectors.toList());
        detailsDto.setVideos(videoDtos);
        
        // 유사 미디어 조회
        MediaDto similarMedia = getSimilarMedia(mediaType, id, 1);
        if (similarMedia != null && similarMedia.getResults() != null) {
            detailsDto.setSimilar(similarMedia.getResults());
        }
        
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
        
        List<GenreDto> genreDtos = new ArrayList<>();
        List<MediaGenre> mediaGenres = mediaGenreRepository.findByMediaIdAndMediaType(mediaId, mediaType);
        
        for (MediaGenre mediaGenre : mediaGenres) {
            Genre genre = mediaGenre.getGenre();
            if (genre != null) {
                GenreDto genreDto = new GenreDto();
                genreDto.setId(genre.getId());
                genreDto.setName(genre.getName());
                genreDtos.add(genreDto);
            }
        }
        
        mediaItemDto.setGenres(genreDtos);
    }
} 