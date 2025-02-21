package com.example.CineHive.service.creditService.movie;

import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.TopMovie;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.TopMovieRepository;
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
    private MovieRepository movieRepository;
    @Autowired
    private TopMovieRepository topmovieRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private MovieActorService movieActorService;
    @Autowired
    private MovieVideoService movieVideoService;
    @Autowired
    private MovieDirectorService movieDirectorService;
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
        saveMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 완료!");
    }

    // Top Rated 영화 자동저장 (매일 새벽 3시)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateTopRatedMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 시작...");
        saveMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 완료!");
    }



    @Transactional
    public void saveMoviesToDatabase() {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/now_playing?language=" + "ko" + "&page=" + "1" + "&api_key=" + apiKey)
                .header("Accept", "application/json")
                .retrieve() 
                .bodyToMono(String.class)
                .block();  // block()을 사용하여 응답을 기다립니다.

        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();

                    // 영화가 이미 존재하는지 확인
                    if (!movieRepository.existsById(movieId)) {
                        Movie movie = new Movie();
                        movie.setId(movieId);
                        movie.setTitle(movieNode.get("title").asText());
                        String overviewText = movieNode.get("overview").asText();
                        movie.setOverview(overviewText);
                        movie.setPosterPath(movieNode.get("poster_path").asText());
                        movie.setGenreIds(objectMapper.convertValue(movieNode.get("genre_ids"), List.class));  // List로 변환
                        movie.setVoteAverage(movieNode.get("vote_average").asDouble());
                        movie.setPopularity(movieNode.get("popularity").asDouble());
                        String releaseDateString = movieNode.get("release_date").asText();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                        movie.setReleaseDate(releaseDate);
                        // 비디오 정보 가져오기 (첫 번째 비디오만)
                        Video video = movieVideoService.getFirstVideoForMovie(movieId);
                        if (video != null) {
                            movie.setVideos(List.of(video)); // 비디오 정보를 리스트로 설정
                        }
                        // 데이터베이스에 저장
                        movieRepository.save(movie);
                        System.out.println("Saved movie: " + movie.getTitle());
                        // 배우 정보 저장
                        movieActorService.saveMovieCredits(movieId);
                        //감독 정보 저장
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

    @Transactional
    public void saveTopRatedMoviesToDatabase() {
        // API를 호출하여 데이터를 가져옵니다.
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/top_rated?language=ko&page=1&api_key=" + apiKey)
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
                    if (!topmovieRepository.existsById(movieId)) {
                        TopMovie topmovie = new TopMovie();
                        topmovie.setId(movieId);
                        topmovie.setTitle(movieNode.get("title").asText());
                        topmovie.setOverview(movieNode.get("overview").asText());
                        topmovie.setPosterPath(movieNode.get("poster_path").asText());
                        topmovie.setBackdropPath(movieNode.get("backdrop_path").asText());
                        topmovie.setVoteAverage(movieNode.get("vote_average").asDouble());
                        topmovie.setVoteCount(movieNode.get("vote_count").asInt());
                        topmovie.setPopularity(movieNode.get("popularity").asDouble());
                        topmovie.setAdult(movieNode.get("adult").asBoolean());
                        String releaseDateString = movieNode.get("release_date").asText();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                        topmovie.setReleaseDate(releaseDate);

                        // TopMovie 데이터베이스에 저장
                        topmovieRepository.save(topmovie);
                        System.out.println("Saved movie: " + topmovie.getTitle());

                        // Movie 객체 생성 및 저장
                        Movie movie = new Movie();
                        movie.setId(movieId);
                        movie.setTitle(topmovie.getTitle());
                        movie.setOverview(topmovie.getOverview());
                        movie.setPosterPath(topmovie.getPosterPath());
                        movie.setVoteAverage(topmovie.getVoteAverage());
                        movie.setPopularity(topmovie.getPopularity());
                        movie.setReleaseDate(topmovie.getReleaseDate());

                        // Movie 데이터베이스에 저장
                        movieRepository.save(movie);
                        System.out.println("Saved movie to Movie table: " + movie.getTitle());
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
                .block();  // block()을 사용하여 응답을 기다립니다.

        List<Movie> movies = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();
                    String posterPath = movieNode.get("poster_path").asText();

                    if(posterPath==null || posterPath.isEmpty()){
                        continue;
                    }
                    Movie movie = new Movie();
                    movie.setId(movieId);
                    movie.setTitle(movieNode.get("title").asText());
                    movie.setOverview(movieNode.get("overview").asText());
                    movie.setPosterPath(posterPath);

                    movie.setGenreIds(objectMapper.convertValue(movieNode.get("genre_ids"), List.class));  // List로 변환
                    movie.setVoteAverage(movieNode.get("vote_average").asDouble());
                    movie.setPopularity(movieNode.get("popularity").asDouble());
                    String releaseDateString = movieNode.get("release_date").asText();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);

                    movie.setReleaseDate(releaseDate);
                    //애니메이션 장르 제외
                    if(movie.getGenreIds().contains(16)){
                        continue;
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
                        //배우 정보
                        movieActorService.saveMovieCredits(movieId);
                        //감독 정보
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