package com.example.CineHive.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "메인 화면용 차트 요약 응답 DTO")
public class ChartSummaryResponse {

    @Schema(description = "인기 영화 목록 (상위 N개)")
    private List<MediaChartDto> popularMovies;

    @Schema(description = "평점 높은 영화 목록 (상위 N개)")
    private List<MediaChartDto> topRatedMovies;

    @Schema(description = "인기 TV 시리즈 목록 (상위 N개)")
    private List<MediaChartDto> popularTvSeries;

    @Schema(description = "평점 높은 TV 시리즈 목록 (상위 N개)")
    private List<MediaChartDto> topRatedTvSeries;

    /**
     * 모든 차트 목록을 인자로 받는 생성자.
     * Controller의 Mono.zip 결과로부터 객체를 생성할 때 사용됩니다.
     */
    public ChartSummaryResponse(List<MediaChartDto> popularMovies,
                                List<MediaChartDto> topRatedMovies,
                                List<MediaChartDto> popularTvSeries,
                                List<MediaChartDto> topRatedTvSeries) {
        this.popularMovies = popularMovies;
        this.topRatedMovies = topRatedMovies;
        this.popularTvSeries = popularTvSeries;
        this.topRatedTvSeries = topRatedTvSeries;
    }
}