package com.example.CineHive.service.credit.movie;

import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.RecommendationMovie;
import com.example.CineHive.repository.videos.movie.MovieRecommendationRepository;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SimilarMovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Autowired
    private MovieRepository movieRepository;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private MovieRecommendationRepository movieRecommendationRepository;

    @Autowired
    private MovieActorService movieActorService;
    @Autowired
    private MovieDirectorService movieDirectorService;

    @Autowired
    private MovieVideoService movieVideoService;

    public SimilarMovieService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    public void saveRecommendedMovies(Long movieId, List<Movie> similarMovies) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found"));

        for (Movie similarMovie : similarMovies) {
            Optional<Movie> existingRecommendedMovie = movieRepository.findById(similarMovie.getId());
            if (existingRecommendedMovie.isPresent()) {
                RecommendationMovie recommendationMovie = new RecommendationMovie();
                recommendationMovie.setMovie(movie);
                recommendationMovie.setRecommendedMovie(existingRecommendedMovie.get());

                movieRecommendationRepository.save(recommendationMovie);
            }
        }
    }

    public List<Movie> getSimilarMovies(Long movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "/similar?api_key=" + apiKey + "&language=ko&page=1";

        String response = webClient.get()
                .uri(url)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<Movie> similarMovies = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (int i = 0; i < Math.min(10, moviesNode.size()); i++) {
                    JsonNode movieNode = moviesNode.get(i);
                    Movie movie = new Movie();
                    movie.setId(movieNode.get("id").asLong());
                    movie.setTitle(movieNode.get("title").asText());
                    movie.setOverview(movieNode.get("overview").asText());
                    movie.setPosterPath(movieNode.get("poster_path").asText());
                    movie.setVoteAverage(movieNode.get("vote_average").asDouble());
                    movie.setPopularity(movieNode.get("popularity").asDouble());

                    String releaseDateString = movieNode.get("release_date").asText();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                    movie.setReleaseDate(releaseDate);

                    String backdropPath = movieNode.get("backdrop_path").asText();
                    movie.setBackDropPath(backdropPath);

                    String movieDetailsResponse = webClient.get()
                            .uri("https://api.themoviedb.org/3/movie/" + movie.getId() + "?api_key=" + apiKey + "&language=ko")
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                    JsonNode runtimeNode = movieDetailsNode.get("runtime");
                    movie.setRuntime(runtimeNode != null && !runtimeNode.isNull() ? runtimeNode.asInt() : 0);

                    movieDirectorService.saveMovieDirectors(movieId);
                    movieActorService.saveMovieCredits(movieId);

                    // 비디오 정보 추가
                    Video video = movieVideoService.getFirstVideoForMovie(movie.getId());
                    movie.setVideos(video != null ? List.of(video) : new ArrayList<>());

                    similarMovies.add(movie);
                }

                similarMovies.sort(Comparator.comparingDouble(Movie::getPopularity).reversed()
                        .thenComparing(Comparator.comparingDouble(Movie::getVoteAverage).reversed())
                        .thenComparing(Comparator.comparing(Movie::getReleaseDate).reversed()));

            } catch (Exception e) {
                e.printStackTrace();
            }

            saveRecommendedMovies(movieId, similarMovies);
        }

        return similarMovies;
    }
}
