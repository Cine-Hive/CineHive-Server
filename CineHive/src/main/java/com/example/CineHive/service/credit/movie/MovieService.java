package com.example.CineHive.service.credit.movie;

import com.example.CineHive.Async.AsyncHelper;
import com.example.CineHive.entity.credit.movie.Genre;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.NowPlayingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;



import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    @Autowired
    private WebClient webClient;

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
    private SimilarMovieService similarMovieService;

    private final AsyncHelper asyncHelper;

    @Cacheable(value = "searchMovies", key = "#query", unless = "#result == null or #result.size() == 0")
    public List<Movie> searchMovies(String query) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/search/movie?query="
                        + UriUtils.encode(query, StandardCharsets.UTF_8)
                        + "&api_key=" + apiKey
                        + "&include_adult=true&language=ko&page=1")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<Movie> movies = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                List<Movie> newMovies = new ArrayList<>();

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();
                    String posterPath = movieNode.get("poster_path").asText();
                    String backdropPath = movieNode.get("backdrop_path").asText();

                    if (posterPath == null || posterPath.isEmpty()) continue;

                    if (movieRepository.existsById(movieId)) {
                        movies.add(movieRepository.findById(movieId).get());
                        continue;
                    }

                    Movie movie = new Movie();
                    movie.setId(movieId);
                    movie.setTitle(movieNode.get("title").asText());
                    movie.setOverview(movieNode.get("overview").asText());
                    movie.setPosterPath(posterPath);
                    movie.setBackDropPath(backdropPath);

                    List<Genre> genres = new ArrayList<>();
                    for (JsonNode genreIdNode : movieNode.get("genre_ids")) {
                        Genre genre = new Genre();
                        genre.setId(genreIdNode.asInt());
                        genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                        genres.add(genre);
                    }
                    movie.setGenres(genres);

                    if (genres.stream().anyMatch(g -> g.getId() == 16)) continue;

                    movie.setVoteAverage(movieNode.get("vote_average").asDouble());
                    movie.setPopularity(movieNode.get("popularity").asDouble());

                    String releaseDateString = movieNode.get("release_date").asText();
                    if (releaseDateString != null && !releaseDateString.isEmpty()) {
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        movie.setReleaseDate(releaseDate);
                    }

                    String movieDetailsResponse = webClient.get()
                            .uri("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey + "&language=ko")
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

                    JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                    JsonNode runtimeNode = movieDetailsNode.get("runtime");
                    movie.setRuntime(runtimeNode != null && !runtimeNode.isNull() ? runtimeNode.asInt() : 0);

                    Video video = movieVideoService.getFirstVideoForMovie(movieId);
                    movie.setVideos(video != null ? List.of(video) : new ArrayList<>());

                    newMovies.add(movie);
                    movies.add(movie);

                    // 비동기로 추천/감독/배우 저장
                    asyncHelper.saveAdditionalMovieData(movieId);
                }

                // 배치 저장
                movieRepository.saveAll(newMovies);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return movies;
    }

}