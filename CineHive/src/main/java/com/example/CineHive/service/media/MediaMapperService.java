package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.*;
import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.entity.media.Video;
import com.example.CineHive.entity.media.Animation;
import com.example.CineHive.entity.media.Media;
import com.example.CineHive.entity.media.MediaGenre;
import com.example.CineHive.entity.media.Movie;
import com.example.CineHive.entity.media.Tv;
import com.example.CineHive.repository.media.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JSON 데이터 및 엔티티 간 변환을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class MediaMapperService {

    private final MovieRepository movieRepository;
    private final TvRepository tvRepository;
    private final GenreRepository genreRepository;
    private final VideoRepository videoRepository;
    private final CastRepository castRepository;
    private final CrewRepository crewRepository;
    private final AnimationRepository animationRepository;
    private final MediaGenreRepository mediaGenreRepository;

    /**
     * JSON 응답을 MediaItemDto로 변환
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
     * JSON 응답에서 비디오 정보를 추출
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
     * JSON 응답에서 출연진 정보를 추출
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
     * JSON 응답에서 제작진 정보를 추출
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
     * Cast 엔티티를 CastDto로 변환
     */
    public CastDto mapCastToCastDto(Cast cast) {
        CastDto dto = new CastDto();
        dto.setId(cast.getId());
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
    public CrewDto mapCrewToCrewDto(Crew crew) {
        CrewDto dto = new CrewDto();
        dto.setId(crew.getId());
        dto.setName(crew.getName());
        dto.setJob(crew.getJob());
        dto.setDepartment(crew.getDepartment());
        dto.setProfilePath(crew.getProfilePath());
        return dto;
    }
    
    /**
     * Video 엔티티를 VideoDto로 변환
     */
    public VideoDto mapVideoToVideoDto(Video video) {
        VideoDto dto = new VideoDto();
        dto.setId(video.getId());
        dto.setName(video.getName());
        dto.setKey(video.getVideoKey());
        dto.setSite(video.getSite());
        dto.setType(video.getType());
        return dto;
    }
    
    /**
     * MediaItemDto를 MovieEntity로 변환하여 저장
     */
    @Transactional
    public void saveMovieEntity(MediaItemDto dto, Media.MediaCategory category) {
        Movie movie = movieRepository.findById(dto.getId())
                .orElse(new Movie(dto.getId()));
        
        setBaseMediaProperties(movie, dto, category);
        movie.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        movie.setOriginalTitle(dto.getOriginalTitle());
        movie.setOriginalLanguage(dto.getOriginalLanguage());
        
        // 날짜 변환
        setReleaseDate(movie, dto.getReleaseDate());
        
        movieRepository.save(movie);
        
        // 장르 정보 저장
        saveGenres(dto, movie.getId(), Media.MediaType.MOVIE);
    }
    
    /**
     * MediaItemDto를 TvEntity로 변환하여 저장
     */
    @Transactional
    public void saveTvEntity(MediaItemDto dto, Media.MediaCategory category) {
        Tv tv = tvRepository.findById(dto.getId())
                .orElse(new Tv(dto.getId()));
        
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
        
        tvRepository.save(tv);
        
        // 장르 정보 저장
        saveGenres(dto, tv.getId(), Media.MediaType.TV);
    }
    
    /**
     * MediaItemDto를 AnimationEntity로 변환하여 저장
     */
    @Transactional
    public void saveAnimationEntity(MediaItemDto dto, Media.MediaCategory category) {
        Animation animation = animationRepository.findById(dto.getId())
                .orElse(new Animation(dto.getId()));
        
        setBaseMediaProperties(animation, dto, category);
        animation.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        animation.setOriginalTitle(dto.getOriginalTitle());
        animation.setOriginalLanguage(dto.getOriginalLanguage());
        
        // 날짜 변환
        setReleaseDate(animation, dto.getReleaseDate());
        
        animationRepository.save(animation);
        
        // 장르 정보 저장
        saveGenres(dto, animation.getId(), Media.MediaType.ANIMATION);
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
    private void saveGenres(MediaItemDto dto, Long mediaId, Media.MediaType mediaType) {
        // 기존 장르 정보 조회 후 삭제 (업데이트를 위해)
        List<MediaGenre> existingGenres = mediaGenreRepository.findByMediaIdAndMediaType(mediaId, mediaType);
        mediaGenreRepository.deleteAll(existingGenres);
        
        if (dto.getGenres() != null) {
            saveGenresFromDtos(dto.getGenres(), mediaId, mediaType);
        } else if (dto.getGenreIds() != null) {
            saveGenresFromIds(dto.getGenreIds(), mediaId, mediaType);
        }
    }

    /**
     * GenreDto 목록에서 장르 정보 저장
     */
    private void saveGenresFromDtos(List<GenreDto> genreDtos, Long mediaId, Media.MediaType mediaType) {
        for (GenreDto genreDto : genreDtos) {
            Optional<Genre> genreOptional = genreRepository.findByIdAndMediaType(
                    genreDto.getId(), Genre.MediaType.valueOf(mediaType.name()));
            
            Genre genre;
            if (genreOptional.isPresent()) {
                genre = genreOptional.get();
            } else {
                genre = createNewGenre(genreDto.getId(), genreDto.getName(), 
                        Genre.MediaType.valueOf(mediaType.name()));
            }
            
            createMediaGenre(mediaId, mediaType, genre);
        }
    }

    /**
     * 장르 ID 목록에서 장르 정보 저장
     */
    private void saveGenresFromIds(List<Integer> genreIds, Long mediaId, Media.MediaType mediaType) {
        for (Integer genreId : genreIds) {
            Optional<Genre> genreOptional = genreRepository.findByIdAndMediaType(
                    genreId, Genre.MediaType.valueOf(mediaType.name()));
            
            if (genreOptional.isPresent()) {
                createMediaGenre(mediaId, mediaType, genreOptional.get());
            } else {
                // 장르 정보 없음, 공통 장르에서 검색
                genreOptional = genreRepository.findByIdAndMediaType(genreId, Genre.MediaType.COMMON);
                
                if (genreOptional.isPresent()) {
                    createMediaGenre(mediaId, mediaType, genreOptional.get());
                }
                // 장르 정보를 찾을 수 없는 경우는 무시
            }
        }
    }

    /**
     * 새 장르 생성
     */
    private Genre createNewGenre(Integer id, String name, Genre.MediaType mediaType) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        genre.setMediaType(mediaType);
        genreRepository.save(genre);
        return genre;
    }

    /**
     * 미디어-장르 연결 생성
     */
    private void createMediaGenre(Long mediaId, Media.MediaType mediaType, Genre genre) {
        MediaGenre mediaGenre = new MediaGenre();
        mediaGenre.setMediaId(mediaId);
        mediaGenre.setMediaType(mediaType);
        mediaGenre.setGenre(genre);
        mediaGenreRepository.save(mediaGenre);
    }
    
    /**
     * 비디오 정보 저장
     */
    public void saveVideos(List<VideoDto> videoDtos, Long mediaId, Media.MediaType mediaType) {
        for (VideoDto videoDto : videoDtos) {
            Video video = videoRepository.findById(videoDto.getId())
                    .orElse(new Video());
            
            video.setId(videoDto.getId());
            video.setName(videoDto.getName());
            video.setVideoKey(videoDto.getKey());
            video.setSite(videoDto.getSite());
            video.setType(videoDto.getType());
            video.setMediaId(mediaId);
            video.setMediaType(mediaType);
            
            videoRepository.save(video);
        }
    }
    
    /**
     * 출연진 정보 저장
     */
    public void saveCast(List<CastDto> castDtos, Long mediaId, Media.MediaType mediaType) {
        for (CastDto castDto : castDtos) {
            Cast cast = new Cast();
            cast.setCastId(castDto.getCastId());
            cast.setPersonId(castDto.getId());
            cast.setName(castDto.getName());
            
            // 캐릭터 이름 처리 - null 체크와 길이 처리
            String character = castDto.getCharacter();
            if (character == null) {
                character = "";
            }
            cast.setCharacter(character);
            
            cast.setProfilePath(castDto.getProfilePath());
            cast.setOrder(castDto.getOrder());
            cast.setMediaId(mediaId);
            cast.setMediaType(mediaType);
            
            castRepository.save(cast);
        }
    }
    
    /**
     * 제작진 정보 저장
     */
    public void saveCrew(List<CrewDto> crewDtos, Long mediaId, Media.MediaType mediaType) {
        for (CrewDto crewDto : crewDtos) {
            Crew crew = new Crew();
            crew.setPersonId(crewDto.getId());
            crew.setName(crewDto.getName());
            crew.setJob(crewDto.getJob());
            crew.setDepartment(crewDto.getDepartment());
            crew.setProfilePath(crewDto.getProfilePath());
            crew.setMediaId(mediaId);
            crew.setMediaType(mediaType);
            
            crewRepository.save(crew);
        }
    }

    /**
     * Movie를 MediaItemDto로 변환
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
     * Tv를 MediaItemDto로 변환
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
     * Animation을 MediaItemDto로 변환
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
        
        dto.setMediaType("animation");
        
        return dto;
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
} 