package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.MediaDto;
import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.entity.media.Video;
import com.example.CineHive.entity.media.Animation;
import com.example.CineHive.entity.media.Media;
import com.example.CineHive.entity.media.Movie;
import com.example.CineHive.entity.media.Tv;
import com.example.CineHive.repository.media.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * JSON 데이터 및 엔티티 간 변환을 처리하는 서비스
 */
@Service
public class MediaMapperService {

    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private TvRepository tvRepository;
    
    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private CastRepository castRepository;
    
    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private AnimationRepository animationRepository;

    /**
     * JSON 노드를 MediaItemDto로 변환
     */
    public MediaDto.MediaItemDto mapJsonToMediaItemDto(JsonNode mediaNode, Media.MediaType mediaType) {
        MediaDto.MediaItemDto dto = new MediaDto.MediaItemDto();
        
        dto.setId(mediaNode.has("id") ? mediaNode.get("id").asLong() : null);
        
        // 미디어 타입별로 다른 필드 처리
        switch (mediaType) {
            case MOVIE:
                dto.setTitle(mediaNode.has("title") ? mediaNode.get("title").asText() : "");
                dto.setReleaseDate(mediaNode.has("release_date") ? mediaNode.get("release_date").asText() : null);
                dto.setOriginalTitle(mediaNode.has("original_title") ? mediaNode.get("original_title").asText() : null);
                break;
            case TV:
                dto.setTitle(mediaNode.has("name") ? mediaNode.get("name").asText() : "");
                dto.setReleaseDate(mediaNode.has("first_air_date") ? mediaNode.get("first_air_date").asText() : null);
                dto.setOriginalTitle(mediaNode.has("original_name") ? mediaNode.get("original_name").asText() : null);
                
                if (mediaNode.has("origin_country")) {
                    List<String> countries = StreamSupport.stream(mediaNode.get("origin_country").spliterator(), false)
                            .map(JsonNode::asText)
                            .collect(Collectors.toList());
                    dto.setOriginCountry(countries);
                }
                
                dto.setNumberOfSeasons(mediaNode.has("number_of_seasons") ? mediaNode.get("number_of_seasons").asInt() : null);
                dto.setNumberOfEpisodes(mediaNode.has("number_of_episodes") ? mediaNode.get("number_of_episodes").asInt() : null);
                dto.setFirstAirDate(mediaNode.has("first_air_date") ? mediaNode.get("first_air_date").asText() : null);
                dto.setLastAirDate(mediaNode.has("last_air_date") ? mediaNode.get("last_air_date").asText() : null);
                break;
            case ANIMATION:
                // 애니메이션은 영화나 TV 형식에 따라 다르게 처리
                if (mediaNode.has("title")) {
                    dto.setTitle(mediaNode.get("title").asText());
                    dto.setReleaseDate(mediaNode.has("release_date") ? mediaNode.get("release_date").asText() : null);
                    dto.setOriginalTitle(mediaNode.has("original_title") ? mediaNode.get("original_title").asText() : null);
                } else {
                    dto.setTitle(mediaNode.has("name") ? mediaNode.get("name").asText() : "");
                    dto.setReleaseDate(mediaNode.has("first_air_date") ? mediaNode.get("first_air_date").asText() : null);
                    dto.setOriginalTitle(mediaNode.has("original_name") ? mediaNode.get("original_name").asText() : null);
                }
                break;
        }
        
        dto.setOverview(mediaNode.has("overview") ? mediaNode.get("overview").asText() : "");
        dto.setPosterPath(mediaNode.has("poster_path") ? mediaNode.get("poster_path").asText() : null);
        dto.setBackdropPath(mediaNode.has("backdrop_path") ? mediaNode.get("backdrop_path").asText() : null);
        dto.setVoteAverage(mediaNode.has("vote_average") ? mediaNode.get("vote_average").asDouble() : 0.0);
        dto.setPopularity(mediaNode.has("popularity") ? mediaNode.get("popularity").asDouble() : 0.0);
        dto.setRuntime(mediaNode.has("runtime") ? mediaNode.get("runtime").asInt() : null);
        dto.setOriginalLanguage(mediaNode.has("original_language") ? mediaNode.get("original_language").asText() : null);
        
        // 장르 정보 처리
        List<MediaDto.GenreDto> genreDtos = new ArrayList<>();
        if (mediaNode.has("genres")) {
            JsonNode genresNode = mediaNode.get("genres");
            for (JsonNode genreNode : genresNode) {
                MediaDto.GenreDto genreDto = new MediaDto.GenreDto();
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
        
        // 비디오 정보 처리
        if (mediaNode.has("videos") && mediaNode.get("videos").has("results")) {
            JsonNode videosNode = mediaNode.get("videos").get("results");
            List<MediaDto.VideoDto> videoDtos = new ArrayList<>();
            
            for (JsonNode videoNode : videosNode) {
                MediaDto.VideoDto videoDto = new MediaDto.VideoDto();
                videoDto.setId(videoNode.get("id").asText());
                videoDto.setName(videoNode.get("name").asText());
                videoDto.setKey(videoNode.get("key").asText());
                videoDto.setSite(videoNode.get("site").asText());
                videoDto.setType(videoNode.get("type").asText());
                videoDtos.add(videoDto);
            }
            
            dto.setVideos(videoDtos);
        }
        
        // 출연진 정보 처리
        if (mediaNode.has("credits")) {
            JsonNode creditsNode = mediaNode.get("credits");
            
            if (creditsNode.has("cast")) {
                List<MediaDto.CastDto> castDtos = new ArrayList<>();
                JsonNode castNode = creditsNode.get("cast");
                
                for (JsonNode actorNode : castNode) {
                    MediaDto.CastDto castDto = new MediaDto.CastDto();
                    castDto.setId(actorNode.get("id").asLong());
                    castDto.setCastId(actorNode.has("cast_id") ? actorNode.get("cast_id").asLong() : null);
                    castDto.setName(actorNode.get("name").asText());
                    castDto.setCharacter(actorNode.get("character").asText());
                    castDto.setProfilePath(actorNode.has("profile_path") ? actorNode.get("profile_path").asText() : null);
                    castDto.setOrder(actorNode.has("order") ? actorNode.get("order").asInt() : null);
                    castDtos.add(castDto);
                }
                
                dto.setCast(castDtos);
            }
            
            if (creditsNode.has("crew")) {
                List<MediaDto.CrewDto> crewDtos = new ArrayList<>();
                JsonNode crewNode = creditsNode.get("crew");
                
                for (JsonNode crewMemberNode : crewNode) {
                    MediaDto.CrewDto crewDto = new MediaDto.CrewDto();
                    crewDto.setId(crewMemberNode.get("id").asLong());
                    crewDto.setName(crewMemberNode.get("name").asText());
                    crewDto.setJob(crewMemberNode.get("job").asText());
                    crewDto.setDepartment(crewMemberNode.get("department").asText());
                    crewDto.setProfilePath(crewMemberNode.has("profile_path") ? crewMemberNode.get("profile_path").asText() : null);
                    crewDtos.add(crewDto);
                }
                
                dto.setCrew(crewDtos);
            }
        }
        
        dto.setMediaType(mediaType.name().toLowerCase());
        
        return dto;
    }
    
    /**
     * Cast 엔티티를 CastDto로 변환
     */
    public MediaDto.CastDto mapCastToCastDto(Cast cast) {
        MediaDto.CastDto dto = new MediaDto.CastDto();
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
    public MediaDto.CrewDto mapCrewToCrewDto(Crew crew) {
        MediaDto.CrewDto dto = new MediaDto.CrewDto();
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
    public MediaDto.VideoDto mapVideoToVideoDto(Video video) {
        MediaDto.VideoDto dto = new MediaDto.VideoDto();
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
    public void saveMovieEntity(MediaDto.MediaItemDto dto, Media.MediaCategory category) {
        Movie movie = movieRepository.findById(dto.getId())
                .orElse(new Movie(dto.getId()));
        
        movie.setTitle(dto.getTitle());
        movie.setOverview(dto.getOverview());
        movie.setPosterPath(dto.getPosterPath());
        movie.setBackDropPath(dto.getBackdropPath());
        movie.setVoteAverage(dto.getVoteAverage());
        movie.setPopularity(dto.getPopularity());
        movie.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        movie.setOriginalTitle(dto.getOriginalTitle());
        movie.setOriginalLanguage(dto.getOriginalLanguage());
        movie.setCategory(category);
        
        // 날짜 변환
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isEmpty()) {
            try {
                movie.setReleaseDate(LocalDate.parse(dto.getReleaseDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (DateTimeParseException e) {
                // 날짜 파싱 오류 처리
            }
        }
        
        movieRepository.save(movie);
        
        // 장르 정보 저장
        saveGenres(dto, movie.getId(), Media.MediaType.MOVIE);
        
        // 비디오 정보 저장
        if (dto.getVideos() != null) {
            saveVideos(dto.getVideos(), movie.getId(), Media.MediaType.MOVIE);
        }
        
        // 출연진 정보 저장
        if (dto.getCast() != null) {
            saveCast(dto.getCast(), movie.getId(), Media.MediaType.MOVIE);
        }
        
        // 제작진 정보 저장
        if (dto.getCrew() != null) {
            saveCrew(dto.getCrew(), movie.getId(), Media.MediaType.MOVIE);
        }
    }
    
    /**
     * MediaItemDto를 TvEntity로 변환하여 저장
     */
    @Transactional
    public void saveTvEntity(MediaDto.MediaItemDto dto, Media.MediaCategory category) {
        Tv tv = tvRepository.findById(dto.getId())
                .orElse(new Tv(dto.getId()));
        
        tv.setTitle(dto.getTitle());
        tv.setOverview(dto.getOverview());
        tv.setPosterPath(dto.getPosterPath());
        tv.setBackDropPath(dto.getBackdropPath());
        tv.setVoteAverage(dto.getVoteAverage());
        tv.setPopularity(dto.getPopularity());
        tv.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        tv.setOriginalName(dto.getOriginalTitle());
        tv.setOriginalLanguage(dto.getOriginalLanguage());
        tv.setCategory(category);
        
        // 날짜 변환
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isEmpty()) {
            try {
                tv.setReleaseDate(LocalDate.parse(dto.getReleaseDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (DateTimeParseException e) {
                // 날짜 파싱 오류 처리
            }
        }
        
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
        
        // 비디오 정보 저장
        if (dto.getVideos() != null) {
            saveVideos(dto.getVideos(), tv.getId(), Media.MediaType.TV);
        }
        
        // 출연진 정보 저장
        if (dto.getCast() != null) {
            saveCast(dto.getCast(), tv.getId(), Media.MediaType.TV);
        }
        
        // 제작진 정보 저장
        if (dto.getCrew() != null) {
            saveCrew(dto.getCrew(), tv.getId(), Media.MediaType.TV);
        }
    }
    
    /**
     * MediaItemDto를 AnimationEntity로 변환하여 저장
     */
    @Transactional
    public void saveAnimationEntity(MediaDto.MediaItemDto dto, Media.MediaCategory category) {
        Animation animation = animationRepository.findById(dto.getId())
                .orElse(new Animation(dto.getId()));
        
        animation.setTitle(dto.getTitle());
        animation.setOverview(dto.getOverview());
        animation.setPosterPath(dto.getPosterPath());
        animation.setBackDropPath(dto.getBackdropPath());
        animation.setVoteAverage(dto.getVoteAverage());
        animation.setPopularity(dto.getPopularity());
        animation.setRuntime(dto.getRuntime() != null ? dto.getRuntime() : 0);
        animation.setOriginalTitle(dto.getOriginalTitle());
        animation.setOriginalLanguage(dto.getOriginalLanguage());
        animation.setCategory(category);
        
        // 날짜 변환
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isEmpty()) {
            try {
                animation.setReleaseDate(LocalDate.parse(dto.getReleaseDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (DateTimeParseException e) {
                // 날짜 파싱 오류 처리
            }
        }
        
        animationRepository.save(animation);
        
        // 장르 정보 저장
        saveGenres(dto, animation.getId(), Media.MediaType.ANIMATION);
        
        // 비디오 정보 저장
        if (dto.getVideos() != null) {
            saveVideos(dto.getVideos(), animation.getId(), Media.MediaType.ANIMATION);
        }
        
        // 출연진 정보 저장
        if (dto.getCast() != null) {
            saveCast(dto.getCast(), animation.getId(), Media.MediaType.ANIMATION);
        }
        
        // 제작진 정보 저장
        if (dto.getCrew() != null) {
            saveCrew(dto.getCrew(), animation.getId(), Media.MediaType.ANIMATION);
        }
    }
    
    /**
     * 장르 정보 저장
     */
    private void saveGenres(MediaDto.MediaItemDto dto, Long mediaId, Media.MediaType mediaType) {
        if (dto.getGenres() != null) {
            for (MediaDto.GenreDto genreDto : dto.getGenres()) {
                Optional<Genre> genreOptional = genreRepository.findByIdAndMediaType(genreDto.getId(), Genre.MediaType.valueOf(mediaType.name()));
                
                if (genreOptional.isPresent()) {
                    Genre genre = genreOptional.get();
                    // MediaGenre 저장 로직 (생략)
                } else {
                    Genre genre = new Genre();
                    genre.setId(genreDto.getId());
                    genre.setName(genreDto.getName());
                    genre.setMediaType(Genre.MediaType.valueOf(mediaType.name()));
                    genreRepository.save(genre);
                    // MediaGenre 저장 로직 (생략)
                }
            }
        } else if (dto.getGenreIds() != null) {
            for (Integer genreId : dto.getGenreIds()) {
                Optional<Genre> genreOptional = genreRepository.findByIdAndMediaType(genreId, Genre.MediaType.valueOf(mediaType.name()));
                
                if (genreOptional.isPresent()) {
                    // MediaGenre 저장 로직 (생략)
                } else {
                    // 장르 정보 없음, TMDB API에서 장르 정보 조회 필요
                }
            }
        }
    }
    
    /**
     * 비디오 정보 저장
     */
    private void saveVideos(List<MediaDto.VideoDto> videoDtos, Long mediaId, Media.MediaType mediaType) {
        for (MediaDto.VideoDto videoDto : videoDtos) {
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
    private void saveCast(List<MediaDto.CastDto> castDtos, Long mediaId, Media.MediaType mediaType) {
        for (MediaDto.CastDto castDto : castDtos) {
            Cast cast = new Cast();
            cast.setCastId(castDto.getCastId());
            cast.setPersonId(castDto.getId());
            cast.setName(castDto.getName());
            cast.setCharacter(castDto.getCharacter());
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
    private void saveCrew(List<MediaDto.CrewDto> crewDtos, Long mediaId, Media.MediaType mediaType) {
        for (MediaDto.CrewDto crewDto : crewDtos) {
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
    public MediaDto.MediaItemDto mapMovieToDto(Movie movie) {
        MediaDto.MediaItemDto dto = new MediaDto.MediaItemDto();
        
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackdropPath(movie.getBackDropPath());
        dto.setVoteAverage(movie.getVoteAverage());
        dto.setPopularity(movie.getPopularity());
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
    public MediaDto.MediaItemDto mapTvToDto(Tv tv) {
        MediaDto.MediaItemDto dto = new MediaDto.MediaItemDto();
        
        dto.setId(tv.getId());
        dto.setTitle(tv.getTitle());
        dto.setOverview(tv.getOverview());
        dto.setPosterPath(tv.getPosterPath());
        dto.setBackdropPath(tv.getBackDropPath());
        dto.setVoteAverage(tv.getVoteAverage());
        dto.setPopularity(tv.getPopularity());
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
    public MediaDto.MediaItemDto mapAnimationToDto(Animation animation) {
        MediaDto.MediaItemDto dto = new MediaDto.MediaItemDto();
        
        dto.setId(animation.getId());
        dto.setTitle(animation.getTitle());
        dto.setOverview(animation.getOverview());
        dto.setPosterPath(animation.getPosterPath());
        dto.setBackdropPath(animation.getBackDropPath());
        dto.setVoteAverage(animation.getVoteAverage());
        dto.setPopularity(animation.getPopularity());
        dto.setRuntime(animation.getRuntime());
        dto.setOriginalTitle(animation.getOriginalTitle());
        dto.setOriginalLanguage(animation.getOriginalLanguage());
        
        if (animation.getReleaseDate() != null) {
            dto.setReleaseDate(animation.getReleaseDate().toString());
        }
        
        dto.setMediaType("animation");
        
        return dto;
    }
} 