package com.example.CineHive.domain.ops.controller;

import com.example.CineHive.datasync.batch.DataSeedingService;
import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ops/batch/test")
@RequiredArgsConstructor
public class TestBatchController {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    @Qualifier("fullSyncJob")
    private Job fullSyncJob;
    
    @Autowired(required = false)
    @Qualifier("optimizedMovieSyncJob")
    private Job optimizedMovieSyncJob;
    
    @Autowired(required = false)
    @Qualifier("highPerformanceMovieSyncJob")
    private Job highPerformanceMovieSyncJob;
    
    @Autowired(required = false)
    @Qualifier("highPerformanceFullSyncJob")
    private Job highPerformanceFullSyncJob;
    
    @Autowired(required = false)
    @Qualifier("tvAndPersonSeedingJob")
    private Job tvAndPersonSeedingJob;
    
    @Autowired(required = false)
    private DataSeedingService dataSeedingService;

    @GetMapping("/query-debug")
    @Transactional(readOnly = true)
    public Map<String, Object> debugQuery() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 전체 카운트 확인
            String countQuery = "SELECT COUNT(w) FROM TmdbWorkQueue w";
            Long totalCount = entityManager.createQuery(countQuery, Long.class).getSingleResult();
            result.put("totalCount", totalCount);
            log.info("Total count in TmdbWorkQueue: {}", totalCount);
            
            // 2. entity_type별 카운트
            String groupQuery = "SELECT w.entityType, COUNT(w) FROM TmdbWorkQueue w GROUP BY w.entityType";
            List<Object[]> typeCounts = entityManager.createQuery(groupQuery, Object[].class).getResultList();
            result.put("typeDistribution", typeCounts);
            for (Object[] row : typeCounts) {
                log.info("EntityType: {}, Count: {}", row[0], row[1]);
            }
            
            // 3. processed 상태별 카운트
            String processedQuery = "SELECT w.processed, COUNT(w) FROM TmdbWorkQueue w GROUP BY w.processed";
            List<Object[]> processedCounts = entityManager.createQuery(processedQuery, Object[].class).getResultList();
            result.put("processedDistribution", processedCounts);
            for (Object[] row : processedCounts) {
                log.info("Processed: {}, Count: {}", row[0], row[1]);
            }
            
            // 4. 실제 movieDetailStep이 사용하는 쿼리 테스트
            String movieQuery = "SELECT w FROM TmdbWorkQueue w WHERE w.entityType = 'MOVIE' AND w.processed = false ORDER BY w.priority DESC, w.tmdbId ASC";
            log.info("Executing movie query: {}", movieQuery);
            
            TypedQuery<TmdbWorkQueue> query = entityManager.createQuery(movieQuery, TmdbWorkQueue.class);
            query.setMaxResults(10);
            List<TmdbWorkQueue> movies = query.getResultList();
            
            result.put("movieQueryCount", movies.size());
            result.put("firstFewMovies", movies.stream()
                    .limit(5)
                    .map(w -> Map.of(
                            "tmdbId", w.getTmdbId(),
                            "entityType", w.getEntityType().toString(),
                            "processed", w.isProcessed()
                    ))
                    .toList());
            
            log.info("Movie query returned {} results", movies.size());
            
            // 5. ENUM 값 직접 확인
            if (!movies.isEmpty()) {
                TmdbWorkQueue first = movies.get(0);
                log.info("First movie - tmdbId: {}, entityType: {}, entityType.class: {}, processed: {}", 
                        first.getTmdbId(), 
                        first.getEntityType(),
                        first.getEntityType().getClass().getName(),
                        first.isProcessed());
            }
            
            // 6. Native Query로도 확인
            String nativeQuery = "SELECT entity_type, processed, COUNT(*) FROM tmdb_work_queue GROUP BY entity_type, processed";
            List<Object[]> nativeResults = entityManager.createNativeQuery(nativeQuery).getResultList();
            result.put("nativeQueryResults", nativeResults);
            
        } catch (Exception e) {
            log.error("Error during query debug", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/reader-test")
    @Transactional(readOnly = true)
    public Map<String, Object> testReader() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // JPA 쿼리에서 문자열 리터럴 사용
            String query1 = "SELECT w FROM TmdbWorkQueue w WHERE w.entityType = 'MOVIE' AND w.processed = false";
            List<TmdbWorkQueue> result1 = entityManager.createQuery(query1, TmdbWorkQueue.class)
                    .setMaxResults(5)
                    .getResultList();
            result.put("stringLiteralQuery", result1.size());
            
            // JPA 쿼리에서 파라미터 사용
            String query2 = "SELECT w FROM TmdbWorkQueue w WHERE w.entityType = :entityType AND w.processed = :processed";
            List<TmdbWorkQueue> result2 = entityManager.createQuery(query2, TmdbWorkQueue.class)
                    .setParameter("entityType", TmdbWorkQueue.EntityType.MOVIE)
                    .setParameter("processed", false)
                    .setMaxResults(5)
                    .getResultList();
            result.put("parameterQuery", result2.size());
            
            // Native 쿼리로 실제 DB 값 확인
            String nativeQuery = "SELECT DISTINCT entity_type FROM tmdb_work_queue LIMIT 10";
            List<String> entityTypes = entityManager.createNativeQuery(nativeQuery).getResultList();
            result.put("distinctEntityTypes", entityTypes);
            
        } catch (Exception e) {
            log.error("Reader test error", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/trigger-batch")
    public Map<String, Object> triggerBatch() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(fullSyncJob, params);
            
            result.put("success", true);
            result.put("message", "Batch job triggered successfully");
            
        } catch (Exception e) {
            log.error("Failed to trigger batch job", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/trigger-optimized-batch")
    public Map<String, Object> triggerOptimizedBatch() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (optimizedMovieSyncJob == null) {
                result.put("success", false);
                result.put("error", "Optimized batch job not available");
                return result;
            }
            
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addLong("batchSize", 100L)
                    .toJobParameters();
            
            jobLauncher.run(optimizedMovieSyncJob, params);
            
            result.put("success", true);
            result.put("message", "Optimized batch job triggered successfully");
            
        } catch (Exception e) {
            log.error("Failed to trigger optimized batch job", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/trigger-high-performance-batch")
    public Map<String, Object> triggerHighPerformanceBatch() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (highPerformanceMovieSyncJob == null) {
                result.put("success", false);
                result.put("error", "High performance batch job not available");
                return result;
            }
            
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(highPerformanceMovieSyncJob, params);
            
            result.put("success", true);
            result.put("message", "High performance batch job triggered successfully");
            
        } catch (Exception e) {
            log.error("Failed to trigger high performance batch job", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/trigger-full-sync-batch")
    public Map<String, Object> triggerFullSyncBatch() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (highPerformanceFullSyncJob == null) {
                result.put("success", false);
                result.put("error", "High performance full sync job not available");
                return result;
            }
            
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(highPerformanceFullSyncJob, params);
            
            result.put("success", true);
            result.put("message", "High performance full sync batch job triggered successfully");
            
        } catch (Exception e) {
            log.error("Failed to trigger high performance full sync batch job", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/seed-tv-person")
    public Map<String, Object> seedTvAndPerson() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (dataSeedingService == null) {
                result.put("success", false);
                result.put("error", "Data seeding service not available");
                return result;
            }
            
            // DataSeedingService를 사용하여 TV와 Person 데이터 시드
            dataSeedingService.seedAllEntityTypes();
            
            // 큐 상태 확인
            Map<String, Map<String, Integer>> queueStatus = dataSeedingService.verifyQueueStatus();
            
            result.put("success", true);
            result.put("message", "TV and Person data seeded successfully");
            result.put("queueStatus", queueStatus);
            
        } catch (Exception e) {
            log.error("Failed to seed TV and Person data", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}