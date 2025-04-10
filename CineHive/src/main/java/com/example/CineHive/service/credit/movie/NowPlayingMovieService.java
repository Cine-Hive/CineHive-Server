package com.example.CineHive.service.credit.movie;

import com.example.CineHive.dto.video.common.VideoDto;
import com.example.CineHive.dto.video.movie.NowPlayingMovieDto;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.entity.credit.movie.nowplaying.NowPlayingMovieGenre;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.NowPlayingMovie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.NowPlayingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class NowPlayingMovieService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private NowPlayingRepository nowPlayingRepository;
    @Autowired
    private MovieDirectorService movieDirectorService;
    @Autowired
    private MovieActorService movieActorService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieGenreService movieGenreService;
    @Autowired
    private MovieVideoService movieVideoService;
    @Autowired
    private SimilarMovieService similarMovieService;

    public NowPlayingMovieService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 현재 상영영화 자동저장 (매일 자정)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateNowPlayingMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 시작...");
        saveNowPlayingMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 완료!");
    }

    @Transactional
    public void saveNowPlayingMoviesToDatabase() {
        String response = fetchFromApi("/movie/now_playing?language=ko&page=1");

        if (response == null) {
            System.out.println("응답이 없습니다.");
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode moviesNode = rootNode.path("results");

            for (JsonNode movieNode : moviesNode) {
                Long movieId = movieNode.get("id").asLong();
                if (nowPlayingRepository.existsById(movieId)) continue;

                NowPlayingMovie nowPlayingMovie = new NowPlayingMovie();
                nowPlayingMovie.setId(movieId);
                nowPlayingMovie.setTitle(getValidText(movieNode.get("title")));
                nowPlayingMovie.setOverview(getValidText(movieNode.get("overview")));
                nowPlayingMovie.setPosterPath(getValidText(movieNode.get("poster_path")));
                nowPlayingMovie.setBackDropPath(getValidText(movieNode.get("backdrop_path")));
                nowPlayingMovie.setVoteAverage(getValidDouble(movieNode.get("vote_average"), 0.0));
                nowPlayingMovie.setPopularity(getValidDouble(movieNode.get("popularity"), 0.0));

                String releaseDateString = getValidText(movieNode.get("release_date"));
                if (!releaseDateString.isEmpty()) {
                    nowPlayingMovie.setReleaseDate(LocalDate.parse(releaseDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                }

                List<NowPlayingMovieGenre> genres = new ArrayList<>();
                for (JsonNode genreNode : movieNode.path("genre_ids")) {
                    NowPlayingMovieGenre genre = new NowPlayingMovieGenre();
                    genre.setId(genreNode.asInt());
                    genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                    genres.add(genre);
                }
                nowPlayingMovie.setGenres(genres);

                String movieDetailsResponse = fetchFromApi("/movie/" + movieId + "?language=ko");
                if (movieDetailsResponse != null) {
                    JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                    JsonNode runtimeNode = movieDetailsNode.get("runtime");
                    nowPlayingMovie.setRuntime(runtimeNode != null && !runtimeNode.isNull() ? runtimeNode.asInt() : 0);
                }

                nowPlayingRepository.save(nowPlayingMovie);
                System.out.println("Saved now playing movie: " + nowPlayingMovie.getTitle());

                // 관련 데이터 저장
                saveMovieWithRecommendations(movieId, nowPlayingMovie);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveMovieWithRecommendations(Long movieId, NowPlayingMovie nowPlayingMovie) {
        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle(nowPlayingMovie.getTitle());
        movie.setOverview(nowPlayingMovie.getOverview());
        movie.setPosterPath(nowPlayingMovie.getPosterPath());
        movie.setBackDropPath(nowPlayingMovie.getBackDropPath());
        movie.setVoteAverage(nowPlayingMovie.getVoteAverage());
        movie.setPopularity(nowPlayingMovie.getPopularity());
        movie.setReleaseDate(nowPlayingMovie.getReleaseDate());
        movie.setRuntime(nowPlayingMovie.getRuntime());

        Video video = movieVideoService.getFirstVideoForMovie(movieId);
        movie.setVideos(video != null ? List.of(video) : new ArrayList<>());


        movieRepository.save(movie);


        movieActorService.saveMovieCredits(movieId);
        movieDirectorService.saveMovieDirectors(movieId);


        List<Movie> similarMovies = similarMovieService.getSimilarMovies(movieId);
        for (Movie similarMovie : similarMovies) {
            if (!movieRepository.existsById(similarMovie.getId())) {
                movieRepository.save(similarMovie);
                System.out.println("Saved recommended movie: " + similarMovie.getTitle());


                movieActorService.saveMovieCredits(similarMovie.getId());
                movieDirectorService.saveMovieDirectors(similarMovie.getId());
            }
        }
    }

    public List<VideoDto> getNowPlayingMovies(Pageable pageable) {
        String response = fetchFromApi("/movie/now_playing?language=ko&page=" + (pageable.getPageNumber() + 1));

        List<VideoDto> moviePosters = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();
                    VideoDto videoDto = new VideoDto(
                            movieId,
                            getValidText(movieNode.get("poster_path")),
                            getValidText(movieNode.get("title")),
                            getValidText(movieNode.get("release_date")),
                            new ArrayList<>()
                    );
                    moviePosters.add(videoDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return moviePosters;
    }

    private String fetchFromApi(String uri) {
        return webClient.get()
                .uri("https://api.themoviedb.org/3" + uri + "&api_key=" + apiKey)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getValidText(JsonNode node) {
        return (node == null || node.isNull() || "null".equals(node.asText())) ? "" : node.asText();
    }

    private double getValidDouble(JsonNode node, double defaultValue) {
        return (node == null || node.isNull()) ? defaultValue : node.asDouble();
    }
}
