package com.example.CineHive.datasync.batch;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.datasync.dto.*;
import com.example.CineHive.datasync.domain.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 모든 엔티티 타입을 위한 고성능 배치 설정
 * - Movie, TV Series, Person 통합 처리
 * - 병렬 처리 및 대량 Bulk Insert
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class HighPerformanceFullSyncConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final TmdbApiClient tmdbApiClient;
    
    // ===== 통합 Full Sync Job =====
    @Bean
    @Primary
    public Job highPerformanceFullSyncJob() {
        return new JobBuilder("highPerformanceFullSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(prepareFullSyncStep())
                .next(processAllMoviesStep())
                .next(processAllTvSeriesStep())
                .next(processAllPersonsStep())
                .next(cleanupFullSyncStep())
                .build();
    }
    
    @Bean
    public Step prepareFullSyncStep() {
        return new StepBuilder("prepareFullSyncStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
                    
                    // FK 제약 조건 일시 비활성화 - 트랜잭션 내에서 실행
                    log.info("Disabling FK constraints for full sync batch processing...");
                    jdbc.execute("ALTER TABLE movie DISABLE TRIGGER ALL;");
                    jdbc.execute("ALTER TABLE tv_series DISABLE TRIGGER ALL;");
                    jdbc.execute("ALTER TABLE person DISABLE TRIGGER ALL;");
                    
                    // 각 엔티티 타입별 처리할 항목 수 확인
                    String sql = """
                        SELECT entity_type, 
                               COUNT(CASE WHEN status='PENDING' THEN 1 END) as pending,
                               COUNT(CASE WHEN status='DONE' THEN 1 END) as done,
                               COUNT(*) as total
                        FROM tmdb_work_queue
                        GROUP BY entity_type
                    """;
                    
                    jdbc.query(sql, rs -> {
                        log.info("Entity: {} - Pending: {}, Done: {}, Total: {}", 
                            rs.getString("entity_type"),
                            rs.getInt("pending"),
                            rs.getInt("done"),
                            rs.getInt("total")
                        );
                    });
                    
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step cleanupFullSyncStep() {
        return new StepBuilder("cleanupFullSyncStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
                    
                    // FK 제약 조건 재활성화
                    log.info("Re-enabling FK constraints...");
                    jdbc.execute("ALTER TABLE movie ENABLE TRIGGER ALL;");
                    jdbc.execute("ALTER TABLE tv_series ENABLE TRIGGER ALL;");
                    jdbc.execute("ALTER TABLE person ENABLE TRIGGER ALL;");
                    
                    // 최종 처리 결과 통계
                    String sql = """
                        SELECT entity_type,
                               COUNT(CASE WHEN status='DONE' THEN 1 END) as completed,
                               COUNT(CASE WHEN status='FAILED' THEN 1 END) as failed,
                               COUNT(CASE WHEN status='PENDING' THEN 1 END) as pending
                        FROM tmdb_work_queue
                        GROUP BY entity_type
                    """;
                    
                    jdbc.query(sql, rs -> {
                        log.info("Final Stats - Entity: {} - Completed: {}, Failed: {}, Pending: {}", 
                            rs.getString("entity_type"),
                            rs.getInt("completed"),
                            rs.getInt("failed"),
                            rs.getInt("pending")
                        );
                    });
                    
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    // ===== MOVIE Processing =====
    @Bean
    public Step processAllMoviesStep() {
        return new StepBuilder("processAllMoviesStep", jobRepository)
                .<Long, MovieDelta>chunk(200, transactionManager)
                .reader(movieReader())
                .processor(movieProcessor())
                .writer(movieWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .taskExecutor(multiThreadExecutor())
                .build();
    }
    
    @Bean
    @StepScope
    public ItemReader<Long> movieReader() {
        return new EntityQueueReader(dataSource, "MOVIE", 1000);
    }
    
    @Bean
    @StepScope
    public ItemProcessor<Long, MovieDelta> movieProcessor() {
        return movieId -> {
            try {
                TmdbMovieDetailResponse response = tmdbApiClient.getMovieDetail(movieId);
                if (response == null) {
                    updateQueueStatus(movieId, "MOVIE", "FAILED", "Movie not found in TMDB");
                    return null;
                }
                return MovieDelta.fromTmdbResponse(response);
            } catch (Exception e) {
                log.error("Error processing movie {}: {}", movieId, e.getMessage());
                updateQueueStatus(movieId, "MOVIE", "FAILED", e.getMessage());
                throw e;
            }
        };
    }
    
    @Bean
    @StepScope
    public ItemWriter<MovieDelta> movieWriter() {
        return new MovieBulkWriter(dataSource);
    }
    
    // ===== TV SERIES Processing =====
    @Bean
    public Step processAllTvSeriesStep() {
        return new StepBuilder("processAllTvSeriesStep", jobRepository)
                .<Long, TvDelta>chunk(200, transactionManager)
                .reader(tvReader())
                .processor(tvProcessor())
                .writer(tvWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .taskExecutor(multiThreadExecutor())
                .build();
    }
    
    @Bean
    @StepScope
    public ItemReader<Long> tvReader() {
        return new EntityQueueReader(dataSource, "TV", 1000);
    }
    
    @Bean
    @StepScope
    public ItemProcessor<Long, TvDelta> tvProcessor() {
        return tvId -> {
            try {
                TmdbTvSeriesDetailResponse response = tmdbApiClient.getTvSeriesDetail(tvId);
                if (response == null) {
                    updateQueueStatus(tvId, "TV", "FAILED", "TV series not found in TMDB");
                    return null;
                }
                return TvDelta.fromTmdbResponse(response);
            } catch (Exception e) {
                log.error("Error processing TV series {}: {}", tvId, e.getMessage());
                updateQueueStatus(tvId, "TV", "FAILED", e.getMessage());
                throw e;
            }
        };
    }
    
    @Bean
    @StepScope
    public ItemWriter<TvDelta> tvWriter() {
        return new TvBulkWriter(dataSource);
    }
    
    // ===== PERSON Processing =====
    @Bean
    public Step processAllPersonsStep() {
        return new StepBuilder("processAllPersonsStep", jobRepository)
                .<Long, PersonDelta>chunk(200, transactionManager)
                .reader(personReader())
                .processor(personProcessor())
                .writer(personWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .taskExecutor(multiThreadExecutor())
                .build();
    }
    
    @Bean
    @StepScope
    public ItemReader<Long> personReader() {
        return new EntityQueueReader(dataSource, "PERSON", 1000);
    }
    
    @Bean
    @StepScope
    public ItemProcessor<Long, PersonDelta> personProcessor() {
        return personId -> {
            try {
                TmdbPersonDetailResponse response = tmdbApiClient.getPersonDetail(personId);
                if (response == null) {
                    updateQueueStatus(personId, "PERSON", "FAILED", "Person not found in TMDB");
                    return null;
                }
                return PersonDelta.fromTmdbResponse(response);
            } catch (Exception e) {
                log.error("Error processing person {}: {}", personId, e.getMessage());
                updateQueueStatus(personId, "PERSON", "FAILED", e.getMessage());
                throw e;
            }
        };
    }
    
    @Bean
    @StepScope
    public ItemWriter<PersonDelta> personWriter() {
        return new PersonBulkWriter(dataSource);
    }
    
    // ===== Common Components =====
    @Bean
    public TaskExecutor multiThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-parallel-");
        executor.initialize();
        return executor;
    }
    
    private void updateQueueStatus(Long tmdbId, String entityType, String status, String error) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update(
            "UPDATE tmdb_work_queue SET status = ?, last_error = ?, updated_at = NOW() WHERE tmdb_id = ? AND entity_type = ?",
            status, error, tmdbId, entityType
        );
    }
    
    // ===== Generic Queue Reader =====
    public static class EntityQueueReader implements ItemReader<Long> {
        private final DataSource dataSource;
        private final String entityType;
        private final int batchSize;
        private final Queue<Long> queue = new ConcurrentLinkedQueue<>();
        private final AtomicInteger batchCount = new AtomicInteger(0);
        private static final int MAX_BATCHES = 100;
        
        public EntityQueueReader(DataSource dataSource, String entityType, int batchSize) {
            this.dataSource = dataSource;
            this.entityType = entityType;
            this.batchSize = batchSize;
        }
        
        @Override
        public Long read() {
            if (queue.isEmpty() && batchCount.get() < MAX_BATCHES) {
                loadNextBatch();
            }
            return queue.poll();
        }
        
        private void loadNextBatch() {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            
            String sql = """
                WITH batch AS (
                    SELECT tmdb_id
                    FROM tmdb_work_queue
                    WHERE entity_type = ?
                      AND status = 'PENDING'
                    ORDER BY priority DESC, tmdb_id
                    FOR UPDATE SKIP LOCKED
                    LIMIT ?
                )
                UPDATE tmdb_work_queue q
                SET status = 'PROCESSING',
                    updated_at = NOW()
                FROM batch
                WHERE q.tmdb_id = batch.tmdb_id
                  AND q.entity_type = ?
                RETURNING q.tmdb_id
            """;
            
            List<Long> ids = jdbc.queryForList(sql, Long.class, entityType, batchSize, entityType);
            queue.addAll(ids);
            
            if (!ids.isEmpty()) {
                batchCount.incrementAndGet();
                log.info("Loaded batch {} for {} with {} items", batchCount.get(), entityType, ids.size());
            }
        }
    }
    
    // ===== Movie Bulk Writer =====
    public static class MovieBulkWriter implements ItemWriter<MovieDelta> {
        private final DataSource dataSource;
        
        public MovieBulkWriter(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        
        @Override
        public void write(Chunk<? extends MovieDelta> chunk) {
            if (chunk.isEmpty()) return;
            
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<? extends MovieDelta> items = chunk.getItems();
            
            processMovieBatch(jdbc, items);
            
            // Update queue status
            String updateSql = "UPDATE tmdb_work_queue SET status = 'DONE', updated_at = NOW() WHERE tmdb_id = ? AND entity_type = 'MOVIE'";
            List<Object[]> updateParams = items.stream()
                .map(delta -> new Object[]{delta.movie().getTmdbId()})
                .collect(Collectors.toList());
            jdbc.batchUpdate(updateSql, updateParams);
            
            log.info("Successfully processed {} movies", items.size());
        }
        
        private void processMovieBatch(JdbcTemplate jdbc, List<? extends MovieDelta> deltas) {
            // 1. Collections 먼저 삽입 (Movie가 참조하므로)
            // Collection ID만 있는 경우도 처리
            Set<Object[]> collectionParams = new HashSet<>();
            Set<Long> collectionIdsOnly = new HashSet<>();
            
            for (MovieDelta delta : deltas) {
                if (delta.collection() != null) {
                    var c = delta.collection();
                    collectionParams.add(new Object[]{
                        c.getTmdbId(), c.getName(), c.getPosterPath(), c.getBackdropPath()
                    });
                } else if (delta.movie().getCollectionId() != null) {
                    // Collection 상세 정보가 없지만 ID만 있는 경우
                    collectionIdsOnly.add(delta.movie().getCollectionId());
                }
            }
            
            // Collection 상세 정보가 있는 경우
            if (!collectionParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO collection (tmdb_id, name, poster_path, backdrop_path, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (tmdb_id) DO UPDATE SET " +
                    "name = COALESCE(EXCLUDED.name, collection.name), " +
                    "poster_path = COALESCE(EXCLUDED.poster_path, collection.poster_path), " +
                    "backdrop_path = COALESCE(EXCLUDED.backdrop_path, collection.backdrop_path), " +
                    "updated_at = NOW()",
                    new ArrayList<>(collectionParams)
                );
            }
            
            // Collection ID만 있는 경우 (placeholder 삽입)
            if (!collectionIdsOnly.isEmpty()) {
                List<Object[]> placeholderParams = collectionIdsOnly.stream()
                    .map(id -> new Object[]{id, "Collection " + id, null, null})
                    .collect(java.util.stream.Collectors.toList());
                    
                jdbc.batchUpdate(
                    "INSERT INTO collection (tmdb_id, name, poster_path, backdrop_path, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (tmdb_id) DO NOTHING",
                    placeholderParams
                );
            }
            
            // 2. Genres
            Set<Object[]> genreParams = new HashSet<>();
            for (MovieDelta delta : deltas) {
                if (delta.genreEntities() != null) {
                    delta.genreEntities().forEach(g -> 
                        genreParams.add(new Object[]{g.getTmdbId(), g.getName()})
                    );
                }
            }
            if (!genreParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO genre (tmdb_id, name) VALUES (?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                    new ArrayList<>(genreParams)
                );
            }
            
            // 3. Keywords
            Set<Object[]> keywordParams = new HashSet<>();
            for (MovieDelta delta : deltas) {
                if (delta.keywordEntities() != null) {
                    delta.keywordEntities().forEach(k -> 
                        keywordParams.add(new Object[]{k.getTmdbId(), k.getName()})
                    );
                }
            }
            if (!keywordParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO keyword (tmdb_id, name) VALUES (?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                    new ArrayList<>(keywordParams)
                );
            }
            
            // 4. Persons (Cast & Crew)
            Set<Long> personIds = new HashSet<>();
            for (MovieDelta delta : deltas) {
                if (delta.cast() != null) {
                    delta.cast().forEach(c -> {
                        if (c.getPersonId() != null) personIds.add(c.getPersonId());
                    });
                }
                if (delta.crew() != null) {
                    delta.crew().forEach(cr -> {
                        if (cr.getPersonId() != null) personIds.add(cr.getPersonId());
                    });
                }
            }
            if (!personIds.isEmpty()) {
                List<Object[]> personParams = personIds.stream()
                    .map(id -> new Object[]{id, "Unknown", null, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now())})
                    .collect(java.util.stream.Collectors.toList());
                jdbc.batchUpdate(
                    "INSERT INTO person (tmdb_id, name, profile_path, updated_from_tmdb_at, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (tmdb_id) DO NOTHING",
                    personParams
                );
            }
            
            // 5. Production Companies
            Set<Object[]> companyParams = new HashSet<>();
            for (MovieDelta delta : deltas) {
                if (delta.companies() != null) {
                    delta.companies().forEach(pc -> 
                        companyParams.add(new Object[]{
                            pc.getTmdbId(), pc.getName(), pc.getLogoPath(), pc.getOriginCountry()
                        })
                    );
                }
            }
            if (!companyParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO production_company (tmdb_id, name, logo_path, origin_country) " +
                    "VALUES (?, ?, ?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                    new ArrayList<>(companyParams)
                );
            }
            
            // 6. Movies (이제 Collection이 이미 존재함)
            List<Object[]> movieParams = deltas.stream().map(delta -> {
                var m = delta.movie();
                return new Object[]{
                    m.getTmdbId(),
                    m.getTitle(),
                    m.getOriginalTitle(),
                    m.getOverview(),
                    m.getReleaseDate() != null ? java.sql.Date.valueOf(m.getReleaseDate()) : null,
                    m.getRuntime(),
                    m.getVoteAverage(),
                    m.getVoteCount(),
                    m.getPopularity(),
                    m.getPosterPath(),
                    m.getBackdropPath(),
                    m.getStatus(),
                    m.getTagline(),
                    m.getBudget(),
                    m.getRevenue(),
                    m.getCollectionId(),
                    Timestamp.from(m.getUpdatedFromTmdbAt().toInstant())
                };
            }).collect(Collectors.toList());
            
            jdbc.batchUpdate(
                "INSERT INTO movie (tmdb_id, title, original_title, overview, release_date, runtime, " +
                "vote_average, vote_count, popularity, poster_path, backdrop_path, status, tagline, " +
                "budget, revenue, collection_id, updated_from_tmdb_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) " +
                "ON CONFLICT (tmdb_id) DO UPDATE SET " +
                "title = EXCLUDED.title, original_title = EXCLUDED.original_title, " +
                "overview = EXCLUDED.overview, release_date = EXCLUDED.release_date, " +
                "runtime = EXCLUDED.runtime, vote_average = EXCLUDED.vote_average, " +
                "vote_count = EXCLUDED.vote_count, popularity = EXCLUDED.popularity, " +
                "poster_path = EXCLUDED.poster_path, backdrop_path = EXCLUDED.backdrop_path, " +
                "status = EXCLUDED.status, tagline = EXCLUDED.tagline, " +
                "budget = EXCLUDED.budget, revenue = EXCLUDED.revenue, " +
                "collection_id = EXCLUDED.collection_id, " +
                "updated_from_tmdb_at = EXCLUDED.updated_from_tmdb_at, updated_at = NOW()",
                movieParams
            );
        }
    }
    
    // ===== TV Bulk Writer =====
    public static class TvBulkWriter implements ItemWriter<TvDelta> {
        private final DataSource dataSource;
        
        public TvBulkWriter(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        
        @Override
        public void write(Chunk<? extends TvDelta> chunk) {
            if (chunk.isEmpty()) return;
            
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<? extends TvDelta> items = chunk.getItems();
            
            processTvBatch(jdbc, items);
            
            // Update queue status
            String updateSql = "UPDATE tmdb_work_queue SET status = 'DONE', updated_at = NOW() WHERE tmdb_id = ? AND entity_type = 'TV'";
            List<Object[]> updateParams = items.stream()
                .map(delta -> new Object[]{delta.tvSeries().getTmdbId()})
                .collect(Collectors.toList());
            jdbc.batchUpdate(updateSql, updateParams);
            
            log.info("Successfully processed {} TV series", items.size());
        }
        
        private void processTvBatch(JdbcTemplate jdbc, List<? extends TvDelta> deltas) {
            // 1. TV Genres 배치 삽입
            Set<Object[]> tvGenreParams = new HashSet<>();
            for (TvDelta delta : deltas) {
                if (delta.genreEntities() != null) {
                    delta.genreEntities().forEach(g -> 
                        tvGenreParams.add(new Object[]{g.getTmdbId(), g.getName()})
                    );
                }
            }
            if (!tvGenreParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO tv_genre (tmdb_id, name) VALUES (?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                    new ArrayList<>(tvGenreParams)
                );
            }
            
            // 2. TV Keywords 배치 삽입
            Set<Object[]> tvKeywordParams = new HashSet<>();
            for (TvDelta delta : deltas) {
                if (delta.keywordEntities() != null) {
                    delta.keywordEntities().forEach(k -> 
                        tvKeywordParams.add(new Object[]{k.getTmdbId(), k.getName()})
                    );
                }
            }
            if (!tvKeywordParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO tv_keyword (tmdb_id, name) VALUES (?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                    new ArrayList<>(tvKeywordParams)
                );
            }
            
            // 3. Networks 배치 삽입
            Set<Object[]> networkParams = new HashSet<>();
            for (TvDelta delta : deltas) {
                if (delta.networks() != null) {
                    delta.networks().forEach(n -> 
                        networkParams.add(new Object[]{
                            n.getTmdbId(), 
                            n.getName(), 
                            n.getLogoPath(), 
                            n.getOriginCountry()
                        })
                    );
                }
            }
            if (!networkParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO network (tmdb_id, name, logo_path, origin_country) " +
                    "VALUES (?, ?, ?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                    new ArrayList<>(networkParams)
                );
            }
            
            // 4. Persons ID 수집 (Cast & Crew에서 추출)
            // Note: TV Series의 경우 Person 상세 정보는 별도 Person 배치에서 처리
            // 여기서는 ID만 수집하여 person 테이블에 플레이스홀더 삽입
            Set<Long> personIds = new HashSet<>();
            for (TvDelta delta : deltas) {
                // Cast에서 Person ID 수집
                if (delta.cast() != null) {
                    delta.cast().forEach(c -> {
                        if (c.getPersonId() != null) {
                            personIds.add(c.getPersonId());
                        }
                    });
                }
                // Crew에서 Person ID 수집
                if (delta.crew() != null) {
                    delta.crew().forEach(cr -> {
                        if (cr.getPersonId() != null) {
                            personIds.add(cr.getPersonId());
                        }
                    });
                }
            }
            if (!personIds.isEmpty()) {
                List<Object[]> personParams = personIds.stream()
                    .map(id -> new Object[]{id, "Unknown", null, Timestamp.from(ZonedDateTime.now().toInstant())})
                    .collect(Collectors.toList());
                    
                jdbc.batchUpdate(
                    "INSERT INTO person (tmdb_id, name, profile_path, updated_from_tmdb_at, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (tmdb_id) DO NOTHING",
                    personParams
                );
            }
            
            // 5. Production Companies 배치 삽입
            Set<Object[]> companyParams = new HashSet<>();
            for (TvDelta delta : deltas) {
                if (delta.companies() != null) {
                    delta.companies().forEach(c -> 
                        companyParams.add(new Object[]{
                            c.getTmdbId(), 
                            c.getName(), 
                            c.getLogoPath(), 
                            c.getOriginCountry()
                        })
                    );
                }
            }
            if (!companyParams.isEmpty()) {
                jdbc.batchUpdate(
                    "INSERT INTO production_company (tmdb_id, name, logo_path, origin_country) " +
                    "VALUES (?, ?, ?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                    new ArrayList<>(companyParams)
                );
            }
            
            // 6. TV Series 배치 삽입
            List<Object[]> tvParams = deltas.stream().map(delta -> {
                var tv = delta.tvSeries();
                return new Object[]{
                    tv.getTmdbId(),
                    tv.getName(),
                    tv.getOriginalName(),
                    tv.getOverview(),
                    tv.getFirstAirDate() != null ? java.sql.Date.valueOf(tv.getFirstAirDate()) : null,
                    tv.getLastAirDate() != null ? java.sql.Date.valueOf(tv.getLastAirDate()) : null,
                    tv.getStatus(),
                    tv.getType(),
                    tv.getVoteAverage(),
                    tv.getVoteCount(),
                    tv.getPopularity(),
                    tv.getPosterPath(),
                    tv.getBackdropPath(),
                    tv.getNumberOfSeasons(),
                    tv.getNumberOfEpisodes(),
                    Timestamp.from(tv.getUpdatedFromTmdbAt().toInstant())
                };
            }).collect(Collectors.toList());
            
            jdbc.batchUpdate(
                "INSERT INTO tv_series (tmdb_id, name, original_name, overview, first_air_date, " +
                "last_air_date, status, type, vote_average, vote_count, popularity, " +
                "poster_path, backdrop_path, number_of_seasons, number_of_episodes, " +
                "updated_from_tmdb_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) " +
                "ON CONFLICT (tmdb_id) DO UPDATE SET " +
                "name = EXCLUDED.name, original_name = EXCLUDED.original_name, " +
                "overview = EXCLUDED.overview, first_air_date = EXCLUDED.first_air_date, " +
                "last_air_date = EXCLUDED.last_air_date, status = EXCLUDED.status, " +
                "type = EXCLUDED.type, vote_average = EXCLUDED.vote_average, " +
                "vote_count = EXCLUDED.vote_count, popularity = EXCLUDED.popularity, " +
                "poster_path = EXCLUDED.poster_path, backdrop_path = EXCLUDED.backdrop_path, " +
                "number_of_seasons = EXCLUDED.number_of_seasons, " +
                "number_of_episodes = EXCLUDED.number_of_episodes, " +
                "updated_from_tmdb_at = EXCLUDED.updated_from_tmdb_at, updated_at = NOW()",
                tvParams
            );
            
            // 7. 관계 테이블들 배치 삽입 (tv_series_genre, tv_series_keyword, tv_cast, tv_crew, tv_series_network)
            for (TvDelta delta : deltas) {
                Long tvId = delta.tvSeries().getTmdbId();
                
                // TV Genres
                if (delta.genres() != null && !delta.genres().isEmpty()) {
                    jdbc.update("DELETE FROM tv_series_genre WHERE tv_series_id = ?", tvId);
                    List<Object[]> tgParams = delta.genres().stream()
                        .map(tg -> new Object[]{tvId, tg.getGenreId()})
                        .collect(Collectors.toList());
                    jdbc.batchUpdate(
                        "INSERT INTO tv_series_genre (tv_series_id, genre_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                        tgParams
                    );
                }
                
                // TV Keywords
                if (delta.keywords() != null && !delta.keywords().isEmpty()) {
                    jdbc.update("DELETE FROM tv_series_keyword WHERE tv_series_id = ?", tvId);
                    List<Object[]> tkParams = delta.keywords().stream()
                        .map(tk -> new Object[]{tvId, tk.getKeywordId()})
                        .collect(Collectors.toList());
                    jdbc.batchUpdate(
                        "INSERT INTO tv_series_keyword (tv_series_id, keyword_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                        tkParams
                    );
                }
                
                // TV Cast
                if (delta.cast() != null && !delta.cast().isEmpty()) {
                    jdbc.update("DELETE FROM tv_cast WHERE tv_series_id = ?", tvId);
                    List<Object[]> tcParams = delta.cast().stream()
                        .map(tc -> new Object[]{
                            tc.getCreditId(), tvId, tc.getPersonId(), 
                            tc.getCharacterName(), tc.getCastOrder()
                        })
                        .collect(Collectors.toList());
                    jdbc.batchUpdate(
                        "INSERT INTO tv_cast (credit_id, tv_series_id, person_id, character_name, cast_order, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                        tcParams
                    );
                }
                
                // TV Crew
                if (delta.crew() != null && !delta.crew().isEmpty()) {
                    jdbc.update("DELETE FROM tv_crew WHERE tv_series_id = ?", tvId);
                    List<Object[]> tcrParams = delta.crew().stream()
                        .map(tcr -> new Object[]{
                            tcr.getCreditId(), tvId, tcr.getPersonId(),
                            tcr.getDepartment(), tcr.getJob()
                        })
                        .collect(Collectors.toList());
                    jdbc.batchUpdate(
                        "INSERT INTO tv_crew (credit_id, tv_series_id, person_id, department, job, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                        tcrParams
                    );
                }
                
                // TV Networks
                if (delta.tvNetworks() != null && !delta.tvNetworks().isEmpty()) {
                    jdbc.update("DELETE FROM tv_series_network WHERE tv_series_id = ?", tvId);
                    List<Object[]> tnParams = delta.tvNetworks().stream()
                        .map(tn -> new Object[]{tvId, tn.getNetworkId()})
                        .collect(Collectors.toList());
                    jdbc.batchUpdate(
                        "INSERT INTO tv_series_network (tv_series_id, network_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                        tnParams
                    );
                }
            }
        }
    }
    
    // ===== Person Bulk Writer =====
    public static class PersonBulkWriter implements ItemWriter<PersonDelta> {
        private final DataSource dataSource;
        
        public PersonBulkWriter(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        
        @Override
        public void write(Chunk<? extends PersonDelta> chunk) {
            if (chunk.isEmpty()) return;
            
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<? extends PersonDelta> items = chunk.getItems();
            
            processPersonBatch(jdbc, items);
            
            // Update queue status
            String updateSql = "UPDATE tmdb_work_queue SET status = 'DONE', updated_at = NOW() WHERE tmdb_id = ? AND entity_type = 'PERSON'";
            List<Object[]> updateParams = items.stream()
                .map(delta -> new Object[]{delta.person().getTmdbId()})
                .collect(Collectors.toList());
            jdbc.batchUpdate(updateSql, updateParams);
            
            log.info("Successfully processed {} persons", items.size());
        }
        
        private void processPersonBatch(JdbcTemplate jdbc, List<? extends PersonDelta> deltas) {
            // Person batch processing
            List<Object[]> personParams = deltas.stream().map(delta -> {
                var p = delta.person();
                return new Object[]{
                    p.getTmdbId(),
                    p.getName(),
                    p.getBiography(),
                    p.getBirthday() != null ? java.sql.Date.valueOf(p.getBirthday()) : null,
                    p.getDeathday() != null ? java.sql.Date.valueOf(p.getDeathday()) : null,
                    p.getGender(),
                    p.getProfilePath(),
                    p.getPopularity(),
                    Timestamp.from(p.getUpdatedFromTmdbAt().toInstant())
                };
            }).collect(Collectors.toList());
            
            jdbc.batchUpdate(
                "INSERT INTO person (tmdb_id, name, biography, birthday, deathday, " +
                "gender, profile_path, popularity, updated_from_tmdb_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) " +
                "ON CONFLICT (tmdb_id) DO UPDATE SET " +
                "name = EXCLUDED.name, biography = EXCLUDED.biography, " +
                "birthday = EXCLUDED.birthday, deathday = EXCLUDED.deathday, " +
                "gender = EXCLUDED.gender, profile_path = EXCLUDED.profile_path, " +
                "popularity = EXCLUDED.popularity, " +
                "updated_from_tmdb_at = EXCLUDED.updated_from_tmdb_at, updated_at = NOW()",
                personParams
            );
        }
    }
}