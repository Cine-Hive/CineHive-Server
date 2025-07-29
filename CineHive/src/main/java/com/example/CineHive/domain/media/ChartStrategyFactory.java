package com.example.CineHive.domain.media;

import com.example.CineHive.domain.media.dto.ChartProperties;
import com.example.CineHive.domain.media.dto.ChartType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ChartStrategyFactory {

    public ChartStrategy getStrategy(ChartType chartType) {
        return switch (chartType) {
            // 기본 영화 차트
            case POPULAR_MOVIES -> (client, page) -> client.getPopularMovies(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case TOP_RATED_MOVIES -> (client, page) -> client.getTopRatedMovies(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case UPCOMING_MOVIES -> (client, page) -> client.getUpcomingMovies(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case NOW_PLAYING_MOVIES -> (client, page) -> client.getNowPlayingMovies(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));

            // 기본 TV 시리즈 차트
            case POPULAR_TV -> (client, page) -> client.getPopularTvSeries(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case TOP_RATED_TV -> (client, page) -> client.getTopRatedTvSeries(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case ON_THE_AIR_TV -> (client, page) -> client.getOnTheAirTvSeries(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case AIRING_TODAY_TV -> (client, page) -> client.getAiringTodayTvSeries(page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));

            // 트렌드 차트
            case TRENDING_MOVIES_WEEK -> (client, page) -> client.getTrendingMovies("week", page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case TRENDING_TV_WEEK -> (client, page) -> client.getTrendingTv("week", page).map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));

            // Discover API 기반 차트
            case ACTION_BLOCKBUSTERS -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("28").sortBy("vote_average.desc").build());
            case SCI_FI_MASTERPIECES -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("878").sortBy("vote_average.desc").build());
            case THRILLER_MUST_WATCH -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("53").sortBy("vote_average.desc").build());
            case ROMANCE_CLASSICS -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("10749").sortBy("vote_average.desc").build());
            case HORROR_TOP_PICKS -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("27").sortBy("vote_average.desc").build());
            case COMEDY_FAVORITES -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("35").sortBy("vote_average.desc").build());
            case DOCUMENTARY_ESSENTIALS -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("99").sortBy("vote_average.desc").build());
            case CRIME_DRAMA_HITS -> createDiscoverTvStrategy(ChartProperties.builder().genreId("80").sortBy("vote_average.desc").build());
            case MARVEL_UNIVERSE_MOVIES -> createDiscoverMovieStrategy(ChartProperties.builder().companyId("420").build());
            case PIXAR_ANIMATION_COLLECTION -> createDiscoverMovieStrategy(ChartProperties.builder().companyId("3").sortBy("vote_average.desc").build());
            case A24_FILMS_SELECTION -> createDiscoverMovieStrategy(ChartProperties.builder().companyId("41077").sortBy("vote_average.desc").build());
            case STUDIO_GHIBLI_MOVIES -> createDiscoverMovieStrategy(ChartProperties.builder().companyId("10342").sortBy("vote_average.desc").build());
            case KOREAN_WAVE_MOVIES -> createDiscoverMovieStrategy(ChartProperties.builder().originCountry("KR").build());
            case KOREAN_DRAMA_SERIES -> createDiscoverTvStrategy(ChartProperties.builder().originCountry("KR").build());
            case JAPANESE_ANIME_SERIES -> createDiscoverTvStrategy(ChartProperties.builder().genreId("16").originCountry("JP").build());
            case TIME_TRAVEL_ADVENTURES -> createDiscoverMovieStrategy(ChartProperties.builder().keywordId("4379").build());
            case ZOMBIE_APOCALYPSE_SURVIVAL -> createDiscoverMovieStrategy(ChartProperties.builder().keywordId("12551").build());
            case CYBERPUNK_FUTURES -> createDiscoverMovieStrategy(ChartProperties.builder().keywordId("3045").build());
            case SUPERHERO_SHOWDOWN -> createDiscoverMovieStrategy(ChartProperties.builder().keywordId("9715").build());
            case FANTASY_EPICS -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("14").sortBy("vote_average.desc").build());
            case ANIMATION_FOR_ADULTS -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("16").keywordId("234326").sortBy("vote_average.desc").build());
            case WAR_AND_HISTORY -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("10752,36").sortBy("vote_average.desc").build());
            case MUSIC_AND_MUSICALS -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("10402").keywordId("4344").sortBy("popularity.desc").build());
            case DC_UNIVERSE_MOVIES -> createDiscoverMovieStrategy(ChartProperties.builder().companyId("128064,9993").sortBy("popularity.desc").build());
            case BLUMHOUSE_HORROR -> createDiscoverMovieStrategy(ChartProperties.builder().companyId("23243").genreId("27").sortBy("popularity.desc").build());
            case WARNER_BROS_ANIMATION -> createDiscoverMovieStrategy(ChartProperties.builder().companyId("174").genreId("16").sortBy("vote_average.desc").build());
            case BRITISH_DRAMA_SERIES -> createDiscoverTvStrategy(ChartProperties.builder().originCountry("GB").genreId("18").sortBy("vote_average.desc").build());
            case FRENCH_CINEMA_SELECTION -> createDiscoverMovieStrategy(ChartProperties.builder().originCountry("FR").sortBy("vote_average.desc").build());
            case INDIAN_BOLLYWOOD_HITS -> createDiscoverMovieStrategy(ChartProperties.builder().originCountry("IN").withOriginalLanguage("hi").sortBy("popularity.desc").build());
            case LEGAL_DRAMA_MOVIES -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("18").keywordId("578").sortBy("vote_average.desc").build());
            case SPY_THRILLER_COLLECTION -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("53,28").keywordId("470").sortBy("popularity.desc").build());
            case POST_APOCALYPSE -> createDiscoverMovieStrategy(ChartProperties.builder().keywordId("4458").sortBy("popularity.desc").build());
            case HIGH_SCHOOL_TEEN_MOVIES -> createDiscoverMovieStrategy(ChartProperties.builder().keywordId("6270").sortBy("popularity.desc").build());
            case SPACE_OPERA -> createDiscoverMovieStrategy(ChartProperties.builder().genreId("878").keywordId("4379-space-opera").sortBy("vote_average.desc").build());
            case HBO_MASTERPIECE_SERIES -> createDiscoverTvStrategy(ChartProperties.builder().networkId("49").sortBy("vote_average.desc").build());
            case NETFLIX_ORIGINAL_SERIES -> createDiscoverTvStrategy(ChartProperties.builder().networkId("213").sortBy("popularity.desc").build());
            case APPLE_TV_ORIGINALS -> createDiscoverTvStrategy(ChartProperties.builder().networkId("2552").sortBy("popularity.desc").build());
            case DISNEY_PLUS_ORIGINALS -> createDiscoverTvStrategy(ChartProperties.builder().networkId("2739").sortBy("popularity.desc").build());
            case Y2K_MOVIES_POPULAR -> createDiscoverMovieStrategy(ChartProperties.builder()
                    .releaseDateGte("2000-01-01")
                    .releaseDateLte("2009-12-31")
                    .sortBy("popularity.desc").build());
            case BEST_OF_2000S_TV -> createDiscoverTvStrategy(ChartProperties.builder()
                    .firstAirDateGte("2000-01-01")
                    .firstAirDateLte("2009-12-31")
                    .sortBy("vote_average.desc").build());
            case RECENTLY_RELEASED_POPULAR -> {
                String oneMonthAgo = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                yield createDiscoverMovieStrategy(ChartProperties.builder()
                        .releaseDateGte(oneMonthAgo)
                        .sortBy("popularity.desc").build());
            }
            case BEST_OF_2023_H2_TV -> createDiscoverTvStrategy(ChartProperties.builder()
                    .firstAirDateGte("2023-07-01")
                    .firstAirDateLte("2023-12-31")
                    .sortBy("vote_average.desc").build());
            case BEST_ANIMATION_OF_THE_YEAR -> createDiscoverMovieStrategy(ChartProperties.builder()
                    .genreId("16")
                    .releaseDateGte("2023-01-01")
                    .releaseDateLte("2023-12-31")
                    .sortBy("vote_average.desc").build());
            case ONE_SEASON_WONDERS -> createDiscoverTvStrategy(ChartProperties.builder()
                    .numberOfSeasons("1")
                    .sortBy("vote_average.desc").build());
            case KOREAN_ACTORS_IN_HOLLYWOOD -> {
                // 배두나(73383), 이병헌(18948), 마동석(97435), 스티븐연(111391), 박서준(240413) 등
                String koreanActorsCastIds = "73383,18948,97435,111391,240413";
                yield createDiscoverMovieStrategy(ChartProperties.builder()
                        .withCast(koreanActorsCastIds)
                        .sortBy("popularity.desc").build());
            }
        };
    }

    private ChartStrategy createDiscoverMovieStrategy(ChartProperties props) {
        return (client, page) -> client.discoverMovies(page, props)
                .map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
    }

    private ChartStrategy createDiscoverTvStrategy(ChartProperties props) {
        return (client, page) -> client.discoverTvSeries(page, props)
                .map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
    }
}