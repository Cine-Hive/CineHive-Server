package com.example.CineHive.service.credit.movie;

import com.example.CineHive.entity.credit.movie.Genre;
import com.example.CineHive.entity.credit.movie.nowplaying.NowPlayingMovieGenre;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.entity.videotype.NowPlayingMovie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.NowPlayingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    @Autowired
    private NowPlayingRepository nowPlayingRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private MovieActorService movieActorService;
    @Autowired
    private MovieVideoService movieVideoService;
    @Autowired
    private MovieDirectorService movieDirectorService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieGenreService movieGenreService;
    @Autowired
    public MovieService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
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
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/now_playing?language=ko&page=1&api_key=" + apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();

                    // 영화가 이미 존재하는지 확인
                    if (!nowPlayingRepository.existsById(movieId)) {
                        NowPlayingMovie nowPlayingMovie = new NowPlayingMovie();
                        nowPlayingMovie.setId(movieId);
                        nowPlayingMovie.setTitle(movieNode.get("title").asText());
                        nowPlayingMovie.setOverview(movieNode.get("overview").asText());
                        nowPlayingMovie.setPosterPath(movieNode.get("poster_path").asText());
                        nowPlayingMovie.setBackDropPath(movieNode.get("backdrop_path").asText());
                        nowPlayingMovie.setVoteAverage(movieNode.get("vote_average").asDouble());
                        nowPlayingMovie.setPopularity(movieNode.get("popularity").asDouble());
                        String releaseDateString = movieNode.get("release_date").asText();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                        nowPlayingMovie.setReleaseDate(releaseDate);


                        List<NowPlayingMovieGenre> genres = new ArrayList<>();
                        for (JsonNode genreNode : movieNode.get("genre_ids")) {
                            NowPlayingMovieGenre genre = new NowPlayingMovieGenre();
                            genre.setId(genreNode.asInt());
                            genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                            genres.add(genre);
                        }
                        nowPlayingMovie.setGenres(genres);


                        String movieDetailsResponse = webClient.get()
                                .uri("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey + "&language=ko")
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                        JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                        JsonNode runtimeNode = movieDetailsNode.get("runtime");
                        if (runtimeNode != null && !runtimeNode.isNull()) {
                            nowPlayingMovie.setRuntime(runtimeNode.asInt());
                        } else {
                            nowPlayingMovie.setRuntime(0);
                        }


                        nowPlayingRepository.save(nowPlayingMovie);
                        System.out.println("Saved now playing movie: " + nowPlayingMovie.getTitle());


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
                        if (video != null) {
                            movie.setVideos(List.of(video));
                        } else {
                            movie.setVideos(new ArrayList<>());
                        }
                        movieRepository.save(movie);
                        System.out.println("Saved movie to Movie table: " + movie.getTitle());


                        movieActorService.saveMovieCredits(movieId);

                        movieDirectorService.saveMovieDirectors(movieId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
    }

    public List<Movie> searchMovies(String query) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/search/movie?query="
                        + UriUtils.encode(query, StandardCharsets.UTF_8)
                        + "&api_key=" + apiKey
                        + "&include_adult=true&language=ko&page=1")
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<Movie> movies = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();
                    String posterPath = movieNode.get("poster_path").asText();

                    if (posterPath == null || posterPath.isEmpty()) {
                        continue;
                    }

                    Movie movie = new Movie();
                    movie.setId(movieId);
                    movie.setTitle(movieNode.get("title").asText());
                    movie.setOverview(movieNode.get("overview").asText());
                    movie.setPosterPath(posterPath);


                    List<Genre> genres = new ArrayList<>();
                    for (JsonNode genreIdNode : movieNode.get("genre_ids")) {
                        Genre genre = new Genre();
                        genre.setId(genreIdNode.asInt());
                        genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                        genres.add(genre);
                    }
                    movie.setGenres(genres);

                    movie.setVoteAverage(movieNode.get("vote_average").asDouble());
                    movie.setPopularity(movieNode.get("popularity").asDouble());
                    String releaseDateString = movieNode.get("release_date").asText();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                    movie.setReleaseDate(releaseDate);

                    // 애니메이션 장르 제외
                    if (movie.getGenres().stream().anyMatch(g -> g.getId() == 16)) {
                        continue;
                    }

                    String movieDetailsResponse = webClient.get()
                            .uri("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey + "&language=ko")
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                    JsonNode runtimeNode = movieDetailsNode.get("runtime");
                    if (runtimeNode != null && !runtimeNode.isNull()) {
                        movie.setRuntime(runtimeNode.asInt());
                    } else {
                        movie.setRuntime(0);
                    }

                    // 비디오 정보 가져오기 (첫 번째 비디오만)
                    Video video = movieVideoService.getFirstVideoForMovie(movieId);
                    if (video != null) {
                        movie.setVideos(List.of(video)); // 비디오 정보를 리스트로 설정
                    } else {
                        movie.setVideos(new ArrayList<>()); // 비디오가 없으면 빈 리스트 설정
                    }

                    // 영화가 데이터베이스에 존재하지 않으면 저장
                    if (!movieRepository.existsById(movieId)) {
                        movieRepository.save(movie);
                        System.out.println("Saved new movie: " + movie.getTitle());
                        // 배우 정보
                        movieActorService.saveMovieCredits(movieId);
                        // 감독 정보
                        movieDirectorService.saveMovieDirectors(movieId);
                    } else {
                        System.out.println("Movie already exists: " + movie.getTitle());
                    }

                    // 데이터베이스와 상관없이 항상 리스트에 추가
                    movies.add(movie);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return movies;
    }

}