package com.example.CineHive.service.credit.movie;

import com.example.CineHive.dto.video.movie.TopRatedMovieDto;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.entity.credit.movie.toprated.topMovieGenre;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.TopMovie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.TopMovieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TopRatedMovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private TopMovieRepository topMovieRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieActorService movieActorService;
    @Autowired
    private MovieDirectorService movieDirectorService;
    @Autowired
    private MovieGenreService movieGenreService;
    @Autowired
    private MovieVideoService movieVideoService;
    @Autowired
    private SimilarMovieService similarMovieService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TopRatedMovieService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    // Top Rated 영화 자동저장 (매일 새벽 3시)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateTopRatedMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 시작...");
        saveTopRatedMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 현재 상영 영화 업데이트 완료!");
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

                    if (!topMovieRepository.existsById(movieId)) {
                        TopMovie topmovie = new TopMovie();
                        topmovie.setId(movieId);
                        topmovie.setTitle(movieNode.get("title").asText());
                        topmovie.setOverview(movieNode.get("overview").asText());
                        topmovie.setPosterPath(movieNode.get("poster_path").asText());
                        topmovie.setBackDropPath(movieNode.get("backdrop_path").asText());
                        topmovie.setVoteAverage(movieNode.get("vote_average").asDouble());
                        topmovie.setPopularity(movieNode.get("popularity").asDouble());
                        String releaseDateString = movieNode.get("release_date").asText();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                        topmovie.setReleaseDate(releaseDate);

                        String movieDetailsResponse = webClient.get()
                                .uri("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey + "&language=ko")
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                        JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                        JsonNode runtimeNode = movieDetailsNode.get("runtime");
                        if (runtimeNode != null && !runtimeNode.isNull()) {
                            topmovie.setRuntime(runtimeNode.asInt());
                        } else {
                            topmovie.setRuntime(0);
                        }

                        List<topMovieGenre> genres = new ArrayList<>();
                        for (JsonNode genreIdNode : movieNode.get("genre_ids")) {
                            topMovieGenre genre = new topMovieGenre();
                            genre.setId(genreIdNode.asInt());
                            genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                            genres.add(genre);
                        }
                        topmovie.setGenres(genres);

                        topMovieRepository.save(topmovie);
                        System.out.println("Saved movie: " + topmovie.getTitle());

                        Movie movie = new Movie();
                        movie.setId(movieId);
                        movie.setTitle(topmovie.getTitle());
                        movie.setOverview(topmovie.getOverview());
                        movie.setPosterPath(topmovie.getPosterPath());
                        movie.setBackDropPath(topmovie.getBackDropPath());
                        movie.setVoteAverage(topmovie.getVoteAverage());
                        movie.setPopularity(topmovie.getPopularity());
                        movie.setReleaseDate(topmovie.getReleaseDate());
                        movie.setRuntime(topmovie.getRuntime());

                        Video video = movieVideoService.getFirstVideoForMovie(movieId);
                        if (video != null) {
                            movie.setVideos(List.of(video));
                        } else {
                            movie.setVideos(new ArrayList<>());
                        }

                        movieRepository.save(movie);

                        movieActorService.saveMovieCredits(movieId);
                        movieDirectorService.saveMovieDirectors(movieId);

                        List<Movie> similarMovies = similarMovieService.getSimilarMovies(movieId);

                        // 추천 영화 저장
                        for (Movie similarMovie : similarMovies) {
                            if (!movieRepository.existsById(similarMovie.getId())) {
                                similarMovie.setBackDropPath(similarMovie.getBackDropPath()); // 필요 시 추가 정보 설정
                                movieRepository.save(similarMovie);
                                System.out.println("Saved recommended movie: " + similarMovie.getTitle());

                                movieActorService.saveMovieCredits(similarMovie.getId());
                                movieDirectorService.saveMovieDirectors(similarMovie.getId());
                            }
                        }

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
    public List<TopRatedMovieDto> getTopRatedMovies(Pageable pageable) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/top_rated?language=ko&page=" + (pageable.getPageNumber() + 1) + "&api_key=" + apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<TopRatedMovieDto> topMovies = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();
                    String posterPath = movieNode.get("poster_path").asText();
                    String title = movieNode.get("title").asText();
                    String releaseDate = movieNode.get("release_date").asText();


                    List<String> genres = new ArrayList<>();
                    if (movieNode.has("genre_ids")) {
                        JsonNode genreIdsNode = movieNode.get("genre_ids");
                        for (JsonNode genreIdNode : genreIdsNode) {
                            genres.add(genreIdNode.asText());
                        }
                    }
                    // DTO 객체를 생성하여 리스트에 추가
                    TopRatedMovieDto topMovieDTO = new TopRatedMovieDto(movieId, posterPath, title, releaseDate, genres);
                    topMovies.add(topMovieDTO);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return topMovies;
    }


}
