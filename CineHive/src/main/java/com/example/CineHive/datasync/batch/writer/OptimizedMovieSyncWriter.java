package com.example.CineHive.datasync.batch.writer;

import com.example.CineHive.datasync.dto.MovieDelta;
import com.example.CineHive.datasync.domain.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JDBC Upsert 기반 최적화된 영화 동기화 Writer
 * JPA 대신 JDBC를 사용하여 데드락과 성능 문제 해결
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptimizedMovieSyncWriter implements ItemWriter<MovieDelta> {
    
    private final DataSource dataSource;
    
    @Override
    @Transactional
    public void write(Chunk<? extends MovieDelta> chunk) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(dataSource);
        
        for (MovieDelta delta : chunk) {
            try {
                Long movieId = delta.movie().getTmdbId();
                log.debug("Processing movie: {}", movieId);
                
                // 1. 참조 엔티티 Upsert (순서 중요!)
                upsertGenres(jdbc, delta.genreEntities());
                upsertKeywords(jdbc, delta.keywordEntities());
                upsertPersons(jdbc, delta.persons());
                upsertProductionCompanies(jdbc, delta.companies());
                upsertCollection(jdbc, delta.collection());
                
                // 2. 영화 본체 Upsert
                upsertMovie(jdbc, delta.movie());
                
                // 3. 관계 테이블 Upsert (중복 무시)
                upsertMovieGenres(namedJdbc, movieId, delta.genres());
                upsertMovieKeywords(namedJdbc, movieId, delta.keywords());
                upsertMovieCast(namedJdbc, movieId, delta.cast());
                upsertMovieCrew(namedJdbc, movieId, delta.crew());
                upsertMovieCompanies(namedJdbc, movieId, delta.movieCompanies());
                
                // 4. 큐 상태 업데이트
                updateQueueStatus(jdbc, movieId, "DONE", null, 0);
                
                log.info("Successfully synced movie: {}", movieId);
                
            } catch (Exception e) {
                log.error("Failed to sync movie: {}", delta.movie().getTmdbId(), e);
                updateQueueStatus(jdbc, delta.movie().getTmdbId(), "FAILED", e.getMessage(), null);
            }
        }
    }
    
    private void upsertGenres(JdbcTemplate jdbc, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;
        
        String sql = """
            INSERT INTO genre (tmdb_id, name, created_at, updated_at)
            VALUES (?, ?, NOW(), NOW())
            ON CONFLICT (tmdb_id) DO UPDATE 
            SET name = EXCLUDED.name,
                updated_at = NOW()
        """;
        
        jdbc.batchUpdate(sql, genres, genres.size(), (ps, genre) -> {
            ps.setLong(1, genre.getTmdbId());
            ps.setString(2, genre.getName());
        });
    }
    
    private void upsertKeywords(JdbcTemplate jdbc, List<Keyword> keywords) {
        if (keywords == null || keywords.isEmpty()) return;
        
        String sql = """
            INSERT INTO keyword (tmdb_id, name, created_at, updated_at)
            VALUES (?, ?, NOW(), NOW())
            ON CONFLICT (tmdb_id) DO UPDATE 
            SET name = EXCLUDED.name,
                updated_at = NOW()
        """;
        
        jdbc.batchUpdate(sql, keywords, keywords.size(), (ps, keyword) -> {
            ps.setLong(1, keyword.getTmdbId());
            ps.setString(2, keyword.getName());
        });
    }
    
    private void upsertPersons(JdbcTemplate jdbc, List<Person> persons) {
        if (persons == null || persons.isEmpty()) return;
        
        String sql = """
            INSERT INTO person (tmdb_id, name, profile_path, updated_from_tmdb_at, created_at, updated_at)
            VALUES (?, ?, ?, ?, NOW(), NOW())
            ON CONFLICT (tmdb_id) DO UPDATE 
            SET name = COALESCE(EXCLUDED.name, person.name),
                profile_path = COALESCE(EXCLUDED.profile_path, person.profile_path),
                updated_from_tmdb_at = EXCLUDED.updated_from_tmdb_at,
                updated_at = NOW()
        """;
        
        jdbc.batchUpdate(sql, persons, persons.size(), (ps, person) -> {
            ps.setLong(1, person.getTmdbId());
            ps.setString(2, person.getName());
            ps.setString(3, person.getProfilePath());
            ps.setTimestamp(4, Timestamp.from(person.getUpdatedFromTmdbAt().toInstant()));
        });
    }
    
    private void upsertProductionCompanies(JdbcTemplate jdbc, List<ProductionCompany> companies) {
        if (companies == null || companies.isEmpty()) return;
        
        String sql = """
            INSERT INTO production_company (tmdb_id, name, logo_path, origin_country, created_at, updated_at)
            VALUES (?, ?, ?, ?, NOW(), NOW())
            ON CONFLICT (tmdb_id) DO UPDATE 
            SET name = COALESCE(EXCLUDED.name, production_company.name),
                logo_path = COALESCE(EXCLUDED.logo_path, production_company.logo_path),
                origin_country = COALESCE(EXCLUDED.origin_country, production_company.origin_country),
                updated_at = NOW()
        """;
        
        jdbc.batchUpdate(sql, companies, companies.size(), (ps, company) -> {
            ps.setLong(1, company.getTmdbId());
            ps.setString(2, company.getName());
            ps.setString(3, company.getLogoPath());
            ps.setString(4, company.getOriginCountry());
        });
    }
    
    private void upsertCollection(JdbcTemplate jdbc, com.example.CineHive.datasync.domain.entity.Collection collection) {
        if (collection == null) return;
        
        String sql = """
            INSERT INTO collection (tmdb_id, name, poster_path, backdrop_path, created_at, updated_at)
            VALUES (?, ?, ?, ?, NOW(), NOW())
            ON CONFLICT (tmdb_id) DO UPDATE 
            SET name = EXCLUDED.name,
                poster_path = EXCLUDED.poster_path,
                backdrop_path = EXCLUDED.backdrop_path,
                updated_at = NOW()
        """;
        
        jdbc.update(sql, 
            collection.getTmdbId(),
            collection.getName(),
            collection.getPosterPath(),
            collection.getBackdropPath()
        );
    }
    
    private void upsertMovie(JdbcTemplate jdbc, Movie movie) {
        String sql = """
            INSERT INTO movie (
                tmdb_id, title, original_title, overview,
                release_date, runtime, vote_average, vote_count, popularity,
                poster_path, backdrop_path, status, tagline,
                budget, revenue, collection_id, updated_from_tmdb_at,
                created_at, updated_at
            ) VALUES (
                ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?,
                NOW(), NOW()
            )
            ON CONFLICT (tmdb_id) DO UPDATE SET
                title = EXCLUDED.title,
                original_title = EXCLUDED.original_title,
                overview = EXCLUDED.overview,
                release_date = EXCLUDED.release_date,
                runtime = EXCLUDED.runtime,
                vote_average = EXCLUDED.vote_average,
                vote_count = EXCLUDED.vote_count,
                popularity = EXCLUDED.popularity,
                poster_path = EXCLUDED.poster_path,
                backdrop_path = EXCLUDED.backdrop_path,
                status = EXCLUDED.status,
                tagline = EXCLUDED.tagline,
                budget = EXCLUDED.budget,
                revenue = EXCLUDED.revenue,
                collection_id = EXCLUDED.collection_id,
                updated_from_tmdb_at = EXCLUDED.updated_from_tmdb_at,
                updated_at = NOW()
        """;
        
        jdbc.update(sql,
            movie.getTmdbId(),
            movie.getTitle(),
            movie.getOriginalTitle(),
            movie.getOverview(),
            movie.getReleaseDate() != null ? java.sql.Date.valueOf(movie.getReleaseDate()) : null,
            movie.getRuntime(),
            movie.getVoteAverage(),
            movie.getVoteCount(),
            movie.getPopularity(),
            movie.getPosterPath(),
            movie.getBackdropPath(),
            movie.getStatus(),
            movie.getTagline(),
            movie.getBudget(),
            movie.getRevenue(),
            movie.getCollectionId(),
            Timestamp.from(movie.getUpdatedFromTmdbAt().toInstant())
        );
    }
    
    private void upsertMovieGenres(NamedParameterJdbcTemplate jdbc, Long movieId, List<MovieGenre> genres) {
        if (genres == null || genres.isEmpty()) return;
        
        String sql = """
            INSERT INTO movie_genre (movie_id, genre_id, created_at, updated_at)
            VALUES (:movieId, :genreId, NOW(), NOW())
            ON CONFLICT (movie_id, genre_id) DO NOTHING
        """;
        
        List<MapSqlParameterSource> params = genres.stream()
            .map(mg -> new MapSqlParameterSource()
                .addValue("movieId", movieId)
                .addValue("genreId", mg.getGenreId()))
            .collect(Collectors.toList());
        
        jdbc.batchUpdate(sql, params.toArray(new MapSqlParameterSource[0]));
    }
    
    private void upsertMovieKeywords(NamedParameterJdbcTemplate jdbc, Long movieId, List<MovieKeyword> keywords) {
        if (keywords == null || keywords.isEmpty()) return;
        
        String sql = """
            INSERT INTO movie_keyword (movie_id, keyword_id, created_at, updated_at)
            VALUES (:movieId, :keywordId, NOW(), NOW())
            ON CONFLICT (movie_id, keyword_id) DO NOTHING
        """;
        
        List<MapSqlParameterSource> params = keywords.stream()
            .map(mk -> new MapSqlParameterSource()
                .addValue("movieId", movieId)
                .addValue("keywordId", mk.getKeywordId()))
            .collect(Collectors.toList());
        
        jdbc.batchUpdate(sql, params.toArray(new MapSqlParameterSource[0]));
    }
    
    private void upsertMovieCast(NamedParameterJdbcTemplate jdbc, Long movieId, List<MovieCast> cast) {
        if (cast == null || cast.isEmpty()) return;
        
        String sql = """
            INSERT INTO movie_cast (
                credit_id, movie_id, person_id, character_name, 
                cast_order, created_at, updated_at
            )
            VALUES (
                :creditId, :movieId, :personId, :character, 
                :order, NOW(), NOW()
            )
            ON CONFLICT (credit_id) DO UPDATE SET
                character_name = EXCLUDED.character_name,
                cast_order = EXCLUDED.cast_order,
                updated_at = NOW()
        """;
        
        List<MapSqlParameterSource> params = cast.stream()
            .map(mc -> new MapSqlParameterSource()
                .addValue("creditId", mc.getCreditId())
                .addValue("movieId", movieId)
                .addValue("personId", mc.getPersonId())
                .addValue("character", mc.getCharacterName())
                .addValue("order", mc.getCastOrder()))
            .collect(Collectors.toList());
        
        jdbc.batchUpdate(sql, params.toArray(new MapSqlParameterSource[0]));
    }
    
    private void upsertMovieCrew(NamedParameterJdbcTemplate jdbc, Long movieId, List<MovieCrew> crew) {
        if (crew == null || crew.isEmpty()) return;
        
        String sql = """
            INSERT INTO movie_crew (
                credit_id, movie_id, person_id, department, 
                job, created_at, updated_at
            )
            VALUES (
                :creditId, :movieId, :personId, :department, 
                :job, NOW(), NOW()
            )
            ON CONFLICT (credit_id) DO UPDATE SET
                department = EXCLUDED.department,
                job = EXCLUDED.job,
                updated_at = NOW()
        """;
        
        List<MapSqlParameterSource> params = crew.stream()
            .map(mc -> new MapSqlParameterSource()
                .addValue("creditId", mc.getCreditId())
                .addValue("movieId", movieId)
                .addValue("personId", mc.getPersonId())
                .addValue("department", mc.getDepartment())
                .addValue("job", mc.getJob()))
            .collect(Collectors.toList());
        
        jdbc.batchUpdate(sql, params.toArray(new MapSqlParameterSource[0]));
    }
    
    private void upsertMovieCompanies(NamedParameterJdbcTemplate jdbc, Long movieId, List<MovieProductionCompany> companies) {
        if (companies == null || companies.isEmpty()) return;
        
        String sql = """
            INSERT INTO movie_production_company (movie_id, company_id, created_at, updated_at)
            VALUES (:movieId, :companyId, NOW(), NOW())
            ON CONFLICT (movie_id, company_id) DO NOTHING
        """;
        
        List<MapSqlParameterSource> params = companies.stream()
            .map(mpc -> new MapSqlParameterSource()
                .addValue("movieId", movieId)
                .addValue("companyId", mpc.getCompanyId()))
            .collect(Collectors.toList());
        
        jdbc.batchUpdate(sql, params.toArray(new MapSqlParameterSource[0]));
    }
    
    private void updateQueueStatus(JdbcTemplate jdbc, Long tmdbId, String status, String errorMsg, Integer retryCount) {
        String sql = """
            UPDATE tmdb_work_queue
            SET status = ?,
                last_error = ?,
                attempts = COALESCE(attempts, 0) + COALESCE(?, 0),
                updated_at = NOW(),
                processed = CASE WHEN ? = 'DONE' THEN true ELSE processed END
            WHERE tmdb_id = ? AND entity_type = 'MOVIE'
        """;
        
        jdbc.update(sql, status, errorMsg, retryCount, status, tmdbId);
    }
}