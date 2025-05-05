package com.example.CineHive.mapper;

import com.example.CineHive.dto.media.*;
import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.*;
import com.example.CineHive.repository.media.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 미디어 데이터 매핑을 위한 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaMapper {
    
    private final GenreRepository genreRepository;
    private final MediaGenreRepository mediaGenreRepository;

    /**
     * TMDB API JSON 응답을 MediaItemDto로 변환
     */
    public MediaItemDto mapJsonToMediaItemDto(JsonNode mediaNode, Media.MediaType mediaType) {
        MediaItemDto dto = new MediaItemDto();

        // 기본 필드 설정
        dto.setId(mediaNode.get("id").asLong());
        dto.setTitle(getTitle(mediaNode));
        dto.setOverview(mediaNode.has("overview") ? mediaNode.get("overview").asText() : "");
        dto.setPosterPath(mediaNode.has("poster_path") ? mediaNode.get("poster_path").asText() : null);
        dto.setBackdropPath(mediaNode.has("backdrop_path") ? mediaNode.get("backdrop_path").asText() : null);
        dto.setVoteAverage(mediaNode.has("vote_average") ? mediaNode.get("vote_average").asDouble() : 0.0);
        dto.setPopularity(mediaNode.has("popularity") ? mediaNode.get("popularity").asDouble() : 0.0);
        dto.setOriginalLanguage(mediaNode.has("original_language") ? mediaNode.get("original_language").asText() : null);
        dto.setOriginalTitle(getOriginalTitle(mediaNode));

        // 개봉일/방영일 처리
        setReleaseDate(dto, mediaNode);

        // TV 전용 필드 처리
        if (mediaType == Media.MediaType.TV || (mediaNode.has("media_type") && mediaNode.get("media_type").asText().equals("tv"))) {
            setTvSpecificFields(dto, mediaNode);
        }

        // 영화 전용 필드 처리
        if (mediaNode.has("runtime")) {
            dto.setRuntime(mediaNode.get("runtime").asInt());
        }

        // 미디어 타입 및 장르 정보 처리
        Media.MediaType determinedType = determineMediaType(mediaNode, mediaType);
        dto.setMediaType(determinedType.name().toLowerCase());

        // 장르 정보 처리
        processGenreInfo(dto, mediaNode);

        return dto;
    }

    /**
     * 미디어 타입 결정 (애니메이션 여부 포함)
     */
    private Media.MediaType determineMediaType(JsonNode mediaNode, Media.MediaType defaultType) {
        // 장르 기반으로 애니메이션 여부 확인 (장르 ID 16이 있으면 애니메이션)
        boolean isAnimation = isAnimationByGenre(mediaNode);

        if (isAnimation) {
            return Media.MediaType.ANIMATION;
        }

        // TMDB 응답의 media_type 필드가 있으면 체크 (검색 결과 등에서 제공)
        if (mediaNode.has("media_type")) {
            String tmdbMediaType = mediaNode.get("media_type").asText();
            if ("movie".equals(tmdbMediaType)) {
                return Media.MediaType.MOVIE;
            } else if ("tv".equals(tmdbMediaType)) {
                return Media.MediaType.TV;
            }
        }

        return defaultType;
    }

    /**
     * 장르 기반으로 애니메이션 여부 확인
     */
    private boolean isAnimationByGenre(JsonNode mediaNode) {
        // 장르 ID 목록에서 16(애니메이션) 확인
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
     * 제목 필드 추출
     */
    private String getTitle(JsonNode mediaNode) {
        return mediaNode.has("title")
            ? mediaNode.get("title").asText()
            : (mediaNode.has("name") ? mediaNode.get("name").asText() : "");
    }

    /**
     * 원제목 필드 추출
     */
    private String getOriginalTitle(JsonNode mediaNode) {
        return mediaNode.has("original_title") 
            ? mediaNode.get("original_title").asText() 
            : (mediaNode.has("original_name") ? mediaNode.get("original_name").asText() : null);
    }

    /**
     * 개봉일/방영일 설정
     */
    private void setReleaseDate(MediaItemDto dto, JsonNode mediaNode) {
        if (mediaNode.has("release_date")) {
            dto.setReleaseDate(mediaNode.get("release_date").asText());
        } else if (mediaNode.has("first_air_date")) {
            dto.setReleaseDate(mediaNode.get("first_air_date").asText());
        }
    }

    /**
     * TV 특정 필드 설정
     */
    private void setTvSpecificFields(MediaItemDto dto, JsonNode mediaNode) {
        if (mediaNode.has("origin_country")) {
            List<String> originCountry = new ArrayList<>();
            JsonNode originCountryNode = mediaNode.get("origin_country");
            if (originCountryNode.isArray()) {
                for (JsonNode countryNode : originCountryNode) {
                    originCountry.add(countryNode.asText());
                }
            }
            dto.setOriginCountry(originCountry);
        }
        
        if (mediaNode.has("first_air_date")) {
            dto.setFirstAirDate(mediaNode.get("first_air_date").asText());
        }
        
        if (mediaNode.has("last_air_date")) {
            dto.setLastAirDate(mediaNode.get("last_air_date").asText());
        }
        
        if (mediaNode.has("number_of_seasons")) {
            dto.setNumberOfSeasons(mediaNode.get("number_of_seasons").asInt());
        }
        
        if (mediaNode.has("number_of_episodes")) {
            dto.setNumberOfEpisodes(mediaNode.get("number_of_episodes").asInt());
        }
    }

    /**
     * 장르 정보 처리
     */
    private void processGenreInfo(MediaItemDto dto, JsonNode mediaNode) {
        if (mediaNode.has("genres")) {
            List<GenreDto> genreDtos = new ArrayList<>();
            JsonNode genresNode = mediaNode.get("genres");
            
            for (JsonNode genreNode : genresNode) {
                GenreDto genreDto = new GenreDto();
                genreDto.setId(genreNode.get("id").asInt());
                genreDto.setName(genreNode.get("name").asText());
                genreDtos.add(genreDto);
            }
            
            dto.setGenres(genreDtos);
        } else if (mediaNode.has("genre_ids")) {
            List<Integer> genreIds = new ArrayList<>();
            JsonNode genreIdsNode = mediaNode.get("genre_ids");
            for (JsonNode genreIdNode : genreIdsNode) {
                genreIds.add(genreIdNode.asInt());
            }
            dto.setGenreIds(genreIds);
        }
    }

    /**
     * JSON 응답에서 비디오 정보 추출
     */
    public List<VideoDto> extractVideosFromJson(JsonNode mediaNode) {
        List<VideoDto> videoDtos = new ArrayList<>();
        
        if (mediaNode.has("videos") && mediaNode.get("videos").has("results")) {
            JsonNode videosNode = mediaNode.get("videos").get("results");
            
            for (JsonNode videoNode : videosNode) {
                VideoDto videoDto = new VideoDto();
                videoDto.setId(videoNode.get("id").asText());
                videoDto.setName(videoNode.get("name").asText());
                videoDto.setKey(videoNode.get("key").asText());
                videoDto.setSite(videoNode.get("site").asText());
                videoDto.setType(videoNode.get("type").asText());
                videoDtos.add(videoDto);
            }
        }
        
        return videoDtos;
    }

    /**
     * JSON 응답에서 출연진 정보 추출
     */
    public List<CastDto> extractCastFromJson(JsonNode mediaNode) {
        List<CastDto> castDtos = new ArrayList<>();
        
        if (mediaNode.has("credits") && mediaNode.get("credits").has("cast")) {
            JsonNode castNode = mediaNode.get("credits").get("cast");
            
            for (JsonNode actorNode : castNode) {
                CastDto castDto = new CastDto();
                castDto.setId(actorNode.get("id").asLong());
                castDto.setCastId(actorNode.has("cast_id") ? actorNode.get("cast_id").asLong() : null);
                castDto.setName(actorNode.get("name").asText());
                castDto.setCharacter(actorNode.get("character").asText());
                castDto.setProfilePath(actorNode.has("profile_path") ? actorNode.get("profile_path").asText() : null);
                castDto.setOrder(actorNode.has("order") ? actorNode.get("order").asInt() : null);
                castDtos.add(castDto);
            }
        }
        
        return castDtos;
    }

    /**
     * JSON 응답에서 제작진 정보 추출
     */
    public List<CrewDto> extractCrewFromJson(JsonNode mediaNode) {
        List<CrewDto> crewDtos = new ArrayList<>();
        
        if (mediaNode.has("credits") && mediaNode.get("credits").has("crew")) {
            JsonNode crewNode = mediaNode.get("credits").get("crew");
            
            for (JsonNode crewMemberNode : crewNode) {
                CrewDto crewDto = new CrewDto();
                crewDto.setId(crewMemberNode.get("id").asLong());
                crewDto.setName(crewMemberNode.get("name").asText());
                crewDto.setJob(crewMemberNode.get("job").asText());
                crewDto.setDepartment(crewMemberNode.get("department").asText());
                crewDto.setProfilePath(crewMemberNode.has("profile_path") ? crewMemberNode.get("profile_path").asText() : null);
                crewDtos.add(crewDto);
            }
        }
        
        return crewDtos;
    }

    /**
     * MediaItemDto를 Movie 엔티티로 변환
     */
    @Transactional
    public Movie mapDtoToMovieEntity(MediaItemDto dto, Media.MediaCategory category) {
        Movie movie = new Movie(dto.getId());

        setBaseMediaProperties(movie, dto, category);
        movie.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        movie.setOriginalTitle(dto.getOriginalTitle());
        movie.setOriginalLanguage(dto.getOriginalLanguage());

        // 날짜 변환
        setReleaseDate(movie, dto.getReleaseDate());

        // 장르 정보 저장
        if (dto.getGenres() != null) {
            saveGenres(dto.getGenres(), movie.getId(), Media.MediaType.MOVIE);
        } else if (dto.getGenreIds() != null) {
            saveGenresByIds(dto.getGenreIds(), movie.getId(), Media.MediaType.MOVIE);
        }

        return movie;
    }

    /**
     * MediaItemDto를 Tv 엔티티로 변환
     */
    @Transactional
    public Tv mapDtoToTvEntity(MediaItemDto dto, Media.MediaCategory category) {
        Tv tv = new Tv(dto.getId());

        setBaseMediaProperties(tv, dto, category);
        tv.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        tv.setOriginalName(dto.getOriginalTitle());
        tv.setOriginalLanguage(dto.getOriginalLanguage());

        // 날짜 변환
        setReleaseDate(tv, dto.getReleaseDate());

        // TV 전용 필드 설정
        if (dto.getOriginCountry() != null) {
            tv.setOriginCountry(dto.getOriginCountry());
        }

        tv.setNumberOfSeasons(dto.getNumberOfSeasons() != null ? dto.getNumberOfSeasons() : 0);
        tv.setNumberOfEpisodes(dto.getNumberOfEpisodes() != null ? dto.getNumberOfEpisodes() : 0);
        tv.setFirstAirDate(dto.getFirstAirDate());
        tv.setLastAirDate(dto.getLastAirDate());

        // 장르 정보 저장
        if (dto.getGenres() != null) {
            saveGenres(dto.getGenres(), tv.getId(), Media.MediaType.TV);
        } else if (dto.getGenreIds() != null) {
            saveGenresByIds(dto.getGenreIds(), tv.getId(), Media.MediaType.TV);
        }

        return tv;
    }

    /**
     * MediaItemDto를 Animation 엔티티로 변환
     */
    @Transactional
    public Animation mapDtoToAnimationEntity(MediaItemDto dto, Media.MediaCategory category) {
        Animation animation = new Animation(dto.getId());

        setBaseMediaProperties(animation, dto, category);
        animation.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        animation.setOriginalTitle(dto.getOriginalTitle());
        animation.setOriginalLanguage(dto.getOriginalLanguage());

        // 날짜 변환
        setReleaseDate(animation, dto.getReleaseDate());

        // 원본 미디어 타입 판별 (TV 시리즈인지 영화인지)
        if (dto.getNumberOfSeasons() != null && dto.getNumberOfSeasons() > 0 || 
            "tv".equals(dto.getMediaType())) {
            // TV 시리즈 기반 애니메이션
            animation.setAnimationType(Animation.AnimationType.TV);
            animation.setNumberOfSeasons(dto.getNumberOfSeasons() != null ? dto.getNumberOfSeasons() : 0);
            animation.setNumberOfEpisodes(dto.getNumberOfEpisodes() != null ? dto.getNumberOfEpisodes() : 0);
        } else {
            // 영화 기반 애니메이션
            animation.setAnimationType(Animation.AnimationType.MOVIE);
        }

        // 장르 정보 저장 (애니메이션 장르(16) 제외)
        if (dto.getGenres() != null) {
            saveGenres(dto.getGenres(), animation.getId(), Media.MediaType.ANIMATION);
        } else if (dto.getGenreIds() != null) {
            saveGenresByIds(dto.getGenreIds(), animation.getId(), Media.MediaType.ANIMATION);
        }

        return animation;
    }
    
    /**
     * Movie 엔티티를 MediaItemDto로 변환
     */
    public MediaItemDto mapMovieToDto(Movie movie) {
        MediaItemDto dto = new MediaItemDto();
        
        mapBaseMediaToDto(dto, movie);
        dto.setRuntime(movie.getRuntime());
        dto.setOriginalTitle(movie.getOriginalTitle());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        
        if (movie.getReleaseDate() != null) {
            dto.setReleaseDate(movie.getReleaseDate().toString());
        }
        
        dto.setMediaType("movie");
        
        return dto;
    }
    
    /**
     * Tv 엔티티를 MediaItemDto로 변환
     */
    public MediaItemDto mapTvToDto(Tv tv) {
        MediaItemDto dto = new MediaItemDto();
        
        mapBaseMediaToDto(dto, tv);
        dto.setRuntime(tv.getRuntime());
        dto.setOriginalTitle(tv.getOriginalName());
        dto.setOriginalLanguage(tv.getOriginalLanguage());
        
        if (tv.getReleaseDate() != null) {
            dto.setReleaseDate(tv.getReleaseDate().toString());
        }
        
        dto.setOriginCountry(tv.getOriginCountry());
        dto.setNumberOfSeasons(tv.getNumberOfSeasons());
        dto.setNumberOfEpisodes(tv.getNumberOfEpisodes());
        dto.setFirstAirDate(tv.getFirstAirDate());
        dto.setLastAirDate(tv.getLastAirDate());
        
        dto.setMediaType("tv");
        
        return dto;
    }
    
    /**
     * Animation 엔티티를 MediaItemDto로 변환
     */
    public MediaItemDto mapAnimationToDto(Animation animation) {
        MediaItemDto dto = new MediaItemDto();
        
        mapBaseMediaToDto(dto, animation);
        dto.setRuntime(animation.getRuntime());
        dto.setOriginalTitle(animation.getOriginalTitle());
        dto.setOriginalLanguage(animation.getOriginalLanguage());
        
        if (animation.getReleaseDate() != null) {
            dto.setReleaseDate(animation.getReleaseDate().toString());
        }
        
        if (animation.getAnimationType() == Animation.AnimationType.TV) {
            dto.setNumberOfSeasons(animation.getNumberOfSeasons());
            dto.setNumberOfEpisodes(animation.getNumberOfEpisodes());
        }
        
        dto.setMediaType("animation");
        
        return dto;
    }
    
    /**
     * Cast 엔티티를 CastDto로 변환
     */
    public CastDto mapCastToDto(Cast cast) {
        CastDto dto = new CastDto();
        dto.setId(cast.getPersonId());
        dto.setCastId(cast.getCastId());
        dto.setName(cast.getName());
        dto.setCharacter(cast.getCharacter());
        dto.setProfilePath(cast.getProfilePath());
        dto.setOrder(cast.getOrder());
        return dto;
    }
    
    /**
     * Crew 엔티티를 CrewDto로 변환
     */
    public CrewDto mapCrewToDto(Crew crew) {
        CrewDto dto = new CrewDto();
        dto.setId(crew.getPersonId());
        dto.setName(crew.getName());
        dto.setJob(crew.getJob());
        dto.setDepartment(crew.getDepartment());
        dto.setProfilePath(crew.getProfilePath());
        return dto;
    }
    
    /**
     * Video 엔티티를 VideoDto로 변환
     */
    public VideoDto mapVideoToDto(Video video) {
        VideoDto dto = new VideoDto();
        dto.setId(video.getId());
        dto.setName(video.getName());
        dto.setKey(video.getVideoKey());
        dto.setSite(video.getSite());
        dto.setType(video.getType());
        return dto;
    }
    
    /**
     * CastDto를 Cast 엔티티로 변환
     */
    public Cast mapDtoToCastEntity(CastDto dto, Long mediaId, Media.MediaType mediaType) {
        Cast cast = new Cast();
        cast.setCastId(dto.getCastId());
        cast.setPersonId(dto.getId());
        cast.setName(dto.getName());
        
        // 캐릭터 이름 처리 - null 체크
        String character = dto.getCharacter();
        if (character == null) {
            character = "";
        }
        cast.setCharacter(character);
        
        cast.setProfilePath(dto.getProfilePath());
        cast.setOrder(dto.getOrder());
        cast.setMediaId(mediaId);
        cast.setMediaType(mediaType);
        
        return cast;
    }
    
    /**
     * CrewDto를 Crew 엔티티로 변환
     */
    public Crew mapDtoToCrewEntity(CrewDto dto, Long mediaId, Media.MediaType mediaType) {
        Crew crew = new Crew();
        crew.setPersonId(dto.getId());
        crew.setName(dto.getName());
        crew.setJob(dto.getJob());
        crew.setDepartment(dto.getDepartment());
        crew.setProfilePath(dto.getProfilePath());
        crew.setMediaId(mediaId);
        crew.setMediaType(mediaType);
        
        return crew;
    }
    
    /**
     * VideoDto를 Video 엔티티로 변환
     */
    public Video mapDtoToVideoEntity(VideoDto dto, Long mediaId, Media.MediaType mediaType) {
        Video video = new Video();
        video.setId(dto.getId());
        video.setName(dto.getName());
        video.setVideoKey(dto.getKey());
        video.setSite(dto.getSite());
        video.setType(dto.getType());
        video.setMediaId(mediaId);
        video.setMediaType(mediaType);
        
        return video;
    }
    
    /**
     * 기본 미디어 속성 설정
     */
    private void setBaseMediaProperties(Media media, MediaItemDto dto, Media.MediaCategory category) {
        media.setTitle(dto.getTitle());
        media.setOverview(dto.getOverview());
        media.setPosterPath(dto.getPosterPath());
        media.setBackdropPath(dto.getBackdropPath());
        media.setVoteAverage(dto.getVoteAverage());
        media.setPopularity(dto.getPopularity());
        media.setCategory(category);
    }
    
    /**
     * 기본 미디어 속성을 DTO로 매핑
     */
    private void mapBaseMediaToDto(MediaItemDto dto, Media media) {
        dto.setId(media.getId());
        dto.setTitle(media.getTitle());
        dto.setOverview(media.getOverview());
        dto.setPosterPath(media.getPosterPath());
        dto.setBackdropPath(media.getBackdropPath());
        dto.setVoteAverage(media.getVoteAverage());
        dto.setPopularity(media.getPopularity());
    }
    
    /**
     * 출시일/방영일 설정
     */
    private void setReleaseDate(Media media, String releaseDateStr) {
        if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
            try {
                media.setReleaseDate(LocalDate.parse(releaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (DateTimeParseException e) {
                // 날짜 파싱 오류 처리
            }
        }
    }
    
    /**
     * 장르 정보 저장
     */
    private void saveGenres(List<GenreDto> genreDtos, Long mediaId, Media.MediaType mediaType) {
        for (GenreDto genreDto : genreDtos) {
            // 애니메이션 타입인 경우 장르 ID 16은 저장하지 않음
            if (mediaType == Media.MediaType.ANIMATION && genreDto.getId() == 16) {
                continue;
            }
            
            // 장르 ID로만 조회 (mediaType 고려하지 않음)
            Optional<Genre> genreOptional = genreRepository.findById(genreDto.getId());
            
            Genre genre;
            if (genreOptional.isPresent()) {
                genre = genreOptional.get();
            } else {
                genre = createNewGenre(genreDto.getId(), genreDto.getName());
                // 새로운 장르를 저장하여 영속 상태로 만듭니다
                genre = genreRepository.save(genre);
            }
            
            createMediaGenre(mediaId, mediaType, genre);
        }
    }
    
    /**
     * 장르 ID 목록에서 장르 정보 저장
     */
    private void saveGenresByIds(List<Integer> genreIds, Long mediaId, Media.MediaType mediaType) {
        for (Integer genreId : genreIds) {
            // 애니메이션 타입인 경우 장르 ID 16은 저장하지 않음
            if (mediaType == Media.MediaType.ANIMATION && genreId == 16) {
                continue;
            }
            
            // 장르 ID로만 조회
            Optional<Genre> genreOptional = genreRepository.findById(genreId);
            
            if (genreOptional.isPresent()) {
                createMediaGenre(mediaId, mediaType, genreOptional.get());
            } else {
                // 장르 정보가 없는 경우 기본 장르 생성 및 저장
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName("Genre " + genreId);
                genre.setMediaType(Genre.MediaType.COMMON); // COMMON으로 통일
                genre = genreRepository.save(genre);
                createMediaGenre(mediaId, mediaType, genre);
            }
        }
    }

    /**
     * 새 장르 생성
     */
    private Genre createNewGenre(Integer id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        genre.setMediaType(Genre.MediaType.COMMON); // COMMON으로 통일
        return genre;
    }

    /**
     * 미디어-장르 연결 생성
     */
    private void createMediaGenre(Long mediaId, Media.MediaType mediaType, Genre genre) {
        // 미디어 ID 확인
        if (mediaId == null) {
            log.error("미디어 ID가 null입니다. 장르 연결을 건너뜁니다. genreId: {}", genre.getId());
            return;
        }
        
        MediaGenre mediaGenre = new MediaGenre();
        mediaGenre.setMediaId(mediaId);
        mediaGenre.setMediaType(mediaType);
        mediaGenre.setGenre(genre);
        
        log.info("미디어-장르 연결 생성: 미디어ID={}, 미디어타입={}, 장르ID={}, 장르명={}",
                mediaId, mediaType, genre.getId(), genre.getName());
        
        // 저장 후 결과 확인
        MediaGenre savedMediaGenre = mediaGenreRepository.save(mediaGenre);
        
        if (savedMediaGenre.getMediaId() == null) {
            log.error("미디어-장르 저장 오류: 저장 후에도 미디어ID가 null입니다. mediaId={}, genreId={}",
                    mediaId, genre.getId());
        }
    }

    /**
     * 미디어 상세 정보 DTO 변환
     * 
     * @param media 미디어 엔티티
     * @param videos 비디오 목록
     * @param casts 출연진 목록
     * @param crews 제작진 목록
     * @return 미디어 상세 정보 DTO
     */
    public MediaDetailsDto toMediaDetailsDto(Media media, List<Video> videos, List<Cast> casts, List<Crew> crews) {
        MediaDetailsDto detailsDto = new MediaDetailsDto();
        
        // 기본 정보 설정
        detailsDto.setMediaInfo(toMediaItemDto(media));
        
        // 비디오 정보 설정
        List<VideoDto> videoDtos = videos.stream()
                .map(this::mapVideoToDto)
                .toList();
        detailsDto.setVideos(videoDtos);
        
        // 출연/제작진 정보 설정
        MediaCreditsDto creditsDto = new MediaCreditsDto();
        
        List<CastDto> castDtos = casts.stream()
                .map(this::mapCastToDto)
                .toList();
        creditsDto.setCast(castDtos);
        
        List<CrewDto> crewDtos = crews.stream()
                .map(this::mapCrewToDto)
                .toList();
        creditsDto.setCrew(crewDtos);
        
        detailsDto.setCredits(creditsDto);
        
        return detailsDto;
    }
    
    /**
     * 미디어 엔티티를 DTO로 변환
     * 
     * @param media 미디어 엔티티
     * @return 미디어 아이템 DTO
     */
    public MediaItemDto toMediaItemDto(Media media) {
        if (media instanceof Movie) {
            return mapMovieToDto((Movie) media);
        } else if (media instanceof Tv) {
            return mapTvToDto((Tv) media);
        } else if (media instanceof Animation) {
            return mapAnimationToDto((Animation) media);
        }
        
        // 기본 미디어 정보만 매핑
        MediaItemDto dto = new MediaItemDto();
        mapBaseMediaToDto(dto, media);
        return dto;
    }
    
    /**
     * TMDB API JSON 응답 전체를 MediaDto로 변환
     * 
     * @param jsonNode TMDB API JSON 응답
     * @param mediaType 미디어 타입
     * @return MediaDto 객체
     */
    public MediaPageDto mapJsonToMediaDto(JsonNode jsonNode, Media.MediaType mediaType) {
        MediaPageDto mediaDto = new MediaPageDto();
        
        // 페이지 정보 설정
        mediaDto.setPage(jsonNode.has("page") ? jsonNode.get("page").asInt() : 1);
        mediaDto.setTotalPages(jsonNode.has("total_pages") ? jsonNode.get("total_pages").asInt() : 0);
        mediaDto.setTotalResults(jsonNode.has("total_results") ? jsonNode.get("total_results").asInt() : 0);
        
        // 미디어 아이템 목록 매핑
        List<MediaItemDto> results = new ArrayList<>();
        if (jsonNode.has("results") && jsonNode.get("results").isArray()) {
            for (JsonNode itemNode : jsonNode.get("results")) {
                MediaItemDto itemDto = mapJsonToMediaItemDto(itemNode, mediaType);
                results.add(itemDto);
            }
        }
        
        mediaDto.setResults(results);
        return mediaDto;
    }
} 