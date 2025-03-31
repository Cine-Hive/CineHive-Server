package com.example.CineHive.service.credit.movie;

import com.example.CineHive.entity.credit.movie.Genre;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.NowPlayingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
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

    @Autowired
    public MovieService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    public List<Movie> searchMovies(String query) {
        String response = webClient.get()
                .uri("/search/movie?query=" + UriUtils.encode(query, StandardCharsets.UTF_8) + "&api_key=" + apiKey + "&include_adult=true&language=ko&page=1")
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
                    String posterPath = getValidText(movieNode.get("poster_path"));
                    String backdropPath = getValidText(movieNode.get("backdrop_path"));
                    String title = getValidText(movieNode.get("title"));
                    String overview = getValidText(movieNode.get("overview"));

                    if (posterPath == null) continue;

                    Movie movie = new Movie();
                    movie.setId(movieId);
                    movie.setTitle(title);
                    movie.setOverview(overview);
                    movie.setPosterPath(posterPath);
                    movie.setBackDropPath(backdropPath);

                    List<Genre> genres = new ArrayList<>();
                    for (JsonNode genreIdNode : movieNode.path("genre_ids")) {
                        Genre genre = new Genre();
                        genre.setId(genreIdNode.asInt());
                        genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                        genres.add(genre);
                    }
                    movie.setGenres(genres);

                    movie.setVoteAverage(movieNode.path("vote_average").asDouble(0.0));
                    movie.setPopularity(movieNode.path("popularity").asDouble(0.0));

                    String releaseDateString = getValidText(movieNode.get("release_date"));
                    if (releaseDateString != null) {
                        movie.setReleaseDate(LocalDate.parse(releaseDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    }

                    if (movie.getGenres().stream().anyMatch(g -> g.getId() == 16)) continue;

                    // 상세 정보 요청
                    String movieDetailsResponse = webClient.get()
                            .uri("/movie/" + movieId + "?api_key=" + apiKey + "&language=ko")
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);

                    JsonNode runtimeNode = movieDetailsNode.get("runtime");
                    movie.setRuntime(runtimeNode != null && !runtimeNode.isNull() ? runtimeNode.asInt() : 0);

                    Video video = movieVideoService.getFirstVideoForMovie(movieId);
                    movie.setVideos(video != null ? List.of(video) : new ArrayList<>());

                    if (!movieRepository.existsById(movieId)) {
                        movieRepository.save(movie);
                        System.out.println("Saved new movie: " + movie.getTitle());

                        movieActorService.saveMovieCredits(movieId);
                        movieDirectorService.saveMovieDirectors(movieId);

                        List<Movie> similarMovies = similarMovieService.getSimilarMovies(movieId);
                        for (Movie similarMovie : similarMovies) {
                            if (!movieRepository.existsById(similarMovie.getId())) {
                                movieRepository.save(similarMovie);
                                System.out.println("Saved recommended movie: " + similarMovie.getTitle());
                            }
                        }
                    } else {
                        System.out.println("Movie already exists: " + movie.getTitle());
                    }

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

    private String getValidText(JsonNode node) {
        return (node != null && !node.isNull()) ? node.asText() : null;
    }
}
