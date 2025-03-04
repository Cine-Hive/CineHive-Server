package com.example.CineHive.service.credit.movie;

import com.example.CineHive.dto.video.movie.PopularMovieDto;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.entity.credit.movie.popular.PopularMovieGenre;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.PoPularMovie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.PopularRepository;
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
public class PopularMovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private PopularRepository popularRepository;

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

    public PopularMovieService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }


    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updatePopularMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 인기 영화 업데이트 시작...");
        savePopularMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트] 인기 영화 업데이트 완료!");
    }

    @Transactional
    public void savePopularMoviesToDatabase() {
        // API를 호출하여 데이터를 가져옵니다.
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/popular?language=ko&page=1&api_key=" + apiKey)
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
                    if (!popularRepository.existsById(movieId)) {
                        PoPularMovie popularMovie = new PoPularMovie();
                        popularMovie.setId(movieId);
                        popularMovie.setTitle(movieNode.get("title").asText());
                        popularMovie.setOverview(movieNode.get("overview").asText());
                        popularMovie.setPosterPath(movieNode.get("poster_path").asText());
                        popularMovie.setBackDropPath(movieNode.get("backdrop_path").asText());
                        popularMovie.setVoteAverage(movieNode.get("vote_average").asDouble());
                        popularMovie.setPopularity(movieNode.get("popularity").asDouble());
                        String releaseDateString = movieNode.get("release_date").asText();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                        popularMovie.setReleaseDate(releaseDate);

                        // 영화 상세 정보 가져오기
                        String movieDetailsResponse = webClient.get()
                                .uri("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey + "&language=ko")
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                        JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                        JsonNode runtimeNode = movieDetailsNode.get("runtime");
                        if (runtimeNode != null && !runtimeNode.isNull()) {
                            popularMovie.setRuntime(runtimeNode.asInt());
                        } else {
                            popularMovie.setRuntime(0);
                        }

                        List<PopularMovieGenre> genres = new ArrayList<>();
                        for (JsonNode genreIdNode : movieNode.get("genre_ids")) {
                            PopularMovieGenre genre = new PopularMovieGenre();
                            genre.setId(genreIdNode.asInt());
                            genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                            genres.add(genre);
                        }
                        popularMovie.setGenres(genres);


                        popularRepository.save(popularMovie);
                        System.out.println("Saved movie: " + popularMovie.getTitle());

                        // Movie 객체 생성 및 저장
                        Movie movie = new Movie();
                        movie.setId(movieId);
                        movie.setTitle(popularMovie.getTitle());
                        movie.setOverview(popularMovie.getOverview());
                        movie.setPosterPath(popularMovie.getPosterPath());
                        movie.setBackDropPath(popularMovie.getBackDropPath());
                        movie.setVoteAverage(popularMovie.getVoteAverage());
                        movie.setPopularity(popularMovie.getPopularity());
                        movie.setReleaseDate(popularMovie.getReleaseDate());
                        movie.setRuntime(popularMovie.getRuntime());

                        Video video = movieVideoService.getFirstVideoForMovie(movieId);
                        if (video != null) {
                            movie.setVideos(List.of(video));
                        } else {
                            movie.setVideos(new ArrayList<>());
                        }
                        // Movie 데이터베이스에 저장
                        movieRepository.save(movie);

                        movieActorService.saveMovieCredits(movieId);
                        movieDirectorService.saveMovieDirectors(movieId);
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

    public List<PopularMovieDto> getPopularMovies(Pageable pageable) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/popular?language=ko&page=" + (pageable.getPageNumber() + 1) + "&api_key=" + apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<PopularMovieDto> popularMovies = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();
                    String posterPath = movieNode.get("poster_path").asText();


                    PopularMovieDto popularMovieDto = new PopularMovieDto(movieId, posterPath);
                    popularMovies.add(popularMovieDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return popularMovies;
    }
}
