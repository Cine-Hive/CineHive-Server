package com.example.CineHive.service.credit.movie;

import com.example.CineHive.dto.video.movie.MovieVideoDto;
import com.example.CineHive.entity.credit.movie.Video;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MovieVideoService {
    private final String BASE_URL = "https://api.themoviedb.org/3/movie/";

    @Value("${tmdb.api.key}") // properties 파일에서 API 키를 주입
    private String apiKey;

    private final RestTemplate restTemplate;

    public MovieVideoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Video getFirstVideoForMovie(Long movieId) {
        String url = BASE_URL + movieId + "/videos?api_key=" + apiKey;
        MovieVideoDto videoResponse = restTemplate.getForObject(url, MovieVideoDto.class);

        if (videoResponse != null && videoResponse.getResults() != null && !videoResponse.getResults().isEmpty()) {
            // 첫 번째 비디오만 반환
            Video video = new Video();
            video.setVideoKey(videoResponse.getResults().get(0).getKey());
            video.setName(videoResponse.getResults().get(0).getName());
            return video;
        }
        return null; // 비디오가 없으면 null 반환
    }

}
