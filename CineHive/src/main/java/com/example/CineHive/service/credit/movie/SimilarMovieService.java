package com.example.CineHive.service.credit.movie;

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

import java.util.ArrayList;
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

                // 연결된 추천 영화 저장
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

                    similarMovies.add(movie);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            saveRecommendedMovies(movieId, similarMovies);
        }

        return similarMovies;
    }


}
