package com.example.CineHive.service.credit.movie;

import com.example.CineHive.dto.video.movie.UpComingMovieDto;
import com.example.CineHive.entity.credit.movie.Video;
import com.example.CineHive.entity.credit.movie.upcoming.UpComingMovieGenre;
import com.example.CineHive.entity.videotype.Movie;
import com.example.CineHive.entity.videotype.UpComingMovie;
import com.example.CineHive.repository.videos.movie.MovieRepository;
import com.example.CineHive.repository.videos.movie.UpComingRepository;
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
public class UpComingMovieService {
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private UpComingRepository upComingRepository;

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

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateUpcomingMoviesDaily() {
        String currentTime = LocalDateTime.now().format(formatter);
        System.out.println("[" + currentTime + "] [자동 업데이트] 개봉 예정 영화 업데이트 시작...");
        saveUpComingMoviesToDatabase();
        System.out.println("[" + currentTime + "] [자동 업데이트]개봉 예정 영화 업데이트 완료!");
    }

    @Transactional
    public void saveUpComingMoviesToDatabase() {
        // API를 호출하여 데이터를 가져옵니다.
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/upcoming?language=ko&page=1&api_key=" + apiKey)
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

                    if (!upComingRepository.existsById(movieId)) {
                        UpComingMovie upComingMovie = new UpComingMovie();
                        upComingMovie.setId(movieId);
                        upComingMovie.setTitle(movieNode.get("title").asText());
                        upComingMovie.setOverview(movieNode.get("overview").asText());
                        upComingMovie.setPosterPath(movieNode.get("poster_path").asText());
                        upComingMovie.setBackDropPath(movieNode.get("backdrop_path").asText());
                        upComingMovie.setVoteAverage(movieNode.get("vote_average").asDouble());
                        upComingMovie.setPopularity(movieNode.get("popularity").asDouble());
                        String releaseDateString = movieNode.get("release_date").asText();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate releaseDate = LocalDate.parse(releaseDateString, formatter);
                        upComingMovie.setReleaseDate(releaseDate);

                        String movieDetailsResponse = webClient.get()
                                .uri("https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey + "&language=ko")
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();

                        JsonNode movieDetailsNode = objectMapper.readTree(movieDetailsResponse);
                        JsonNode runtimeNode = movieDetailsNode.get("runtime");
                        if (runtimeNode != null && !runtimeNode.isNull()) {
                            upComingMovie.setRuntime(runtimeNode.asInt());
                        } else {
                            upComingMovie.setRuntime(0);
                        }

                        List<UpComingMovieGenre> genres = new ArrayList<>();
                        for (JsonNode genreIdNode : movieNode.get("genre_ids")) {
                            UpComingMovieGenre genre = new UpComingMovieGenre();
                            genre.setId(genreIdNode.asInt());
                            genre.setName(movieGenreService.getGenreNameById(genre.getId()));
                            genres.add(genre);
                        }
                        upComingMovie.setGenres(genres);


                        upComingRepository.save(upComingMovie);
                        System.out.println("Saved movie: " + upComingMovie.getTitle());

                        Movie movie = new Movie();
                        movie.setId(movieId);
                        movie.setTitle(upComingMovie.getTitle());
                        movie.setOverview(upComingMovie.getOverview());
                        movie.setPosterPath(upComingMovie.getPosterPath());
                        movie.setBackDropPath(upComingMovie.getBackDropPath());
                        movie.setVoteAverage(upComingMovie.getVoteAverage());
                        movie.setPopularity(upComingMovie.getPopularity());
                        movie.setReleaseDate(upComingMovie.getReleaseDate());
                        movie.setRuntime(upComingMovie.getRuntime());

                        Video video = movieVideoService.getFirstVideoForMovie(movieId);
                        if (video != null) {
                            movie.setVideos(List.of(video));
                        } else {
                            movie.setVideos(new ArrayList<>());
                        }

                        movieRepository.save(movie);

                        List<Movie> similarMovies = similarMovieService.getSimilarMovies(movieId);

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

    public UpComingMovieService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
        this.objectMapper = objectMapper;
    }

    public List<UpComingMovieDto> getUpComingMovies(Pageable pageable) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/upcoming?language=ko&page=" + (pageable.getPageNumber() + 1) + "&api_key=" + apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<UpComingMovieDto> upComingMovies = new ArrayList<>();
        if (response != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode moviesNode = rootNode.path("results");

                for (JsonNode movieNode : moviesNode) {
                    Long movieId = movieNode.get("id").asLong();
                    String posterPath = movieNode.get("poster_path").asText();
                    String title = movieNode.get("title").asText();
                    // DTO 객체를 생성하여 리스트에 추가
                    UpComingMovieDto upComingMovieDto = new UpComingMovieDto(movieId, posterPath, title);
                    upComingMovies.add(upComingMovieDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return upComingMovies;
        }
    }
