package com.example.CineHive.service.credit.movie;

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


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
    }

    public List<NowPlayingMovieDto> getNowPlayingMovies(Pageable pageable) {
        String response = webClient.get()
                .uri("https://api.themoviedb.org/3/movie/now_playing?language=ko&page=" + (pageable.getPageNumber() + 1) + "&api_key=" + apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<NowPlayingMovieDto> moviePosters = new ArrayList<>();
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


                    NowPlayingMovieDto nowPlayingMovieDto = new NowPlayingMovieDto(movieId, posterPath, title, releaseDate, genres);
                    moviePosters.add(nowPlayingMovieDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("응답이 없습니다.");
        }
        return moviePosters;
    }


}
