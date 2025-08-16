package com.example.CineHive.datasync.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * TV Series와 Person 데이터를 TMDB Export에서 다운로드하고 큐에 적재하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeedingService {
    
    private final DataSource dataSource;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM_dd_yyyy");
    private static final String EXPORT_BASE_DIR = "/data/tmdb/exports";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * TV Series 데이터를 다운로드하고 큐에 적재
     */
    @Transactional
    public void seedTvSeriesData() {
        try {
            String fileDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            Path exportDir = Paths.get(EXPORT_BASE_DIR, fileDate);
            Files.createDirectories(exportDir);
            
            log.info("Starting TV Series data seeding for date: {}", fileDate);
            processEntityType("tv_series", "TV", fileDate, exportDir);
            
        } catch (Exception e) {
            log.error("Failed to seed TV Series data", e);
            throw new RuntimeException("Failed to seed TV Series data", e);
        }
    }
    
    /**
     * Person 데이터를 다운로드하고 큐에 적재
     */
    @Transactional
    public void seedPersonData() {
        try {
            String fileDate = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            Path exportDir = Paths.get(EXPORT_BASE_DIR, fileDate);
            Files.createDirectories(exportDir);
            
            log.info("Starting Person data seeding for date: {}", fileDate);
            processEntityType("person", "PERSON", fileDate, exportDir);
            
        } catch (Exception e) {
            log.error("Failed to seed Person data", e);
            throw new RuntimeException("Failed to seed Person data", e);
        }
    }
    
    /**
     * 모든 엔티티 타입의 데이터를 시드
     */
    public void seedAllEntityTypes() {
        log.info("Starting full data seeding for TV and Person entities");
        
        // TV Series 데이터 시드
        seedTvSeriesData();
        
        // Person 데이터 시드
        seedPersonData();
        
        // 결과 확인
        verifyQueueStatus();
    }
    
    private void processEntityType(String fileType, String entityType, String fileDate, Path exportDir) throws Exception {
        // 1. 다운로드
        String url = String.format("http://files.tmdb.org/p/exports/%s_ids_%s.json.gz", fileType, fileDate);
        Path gzFile = exportDir.resolve(String.format("%s_ids_%s.json.gz", fileType, fileDate));
        Path jsonFile = exportDir.resolve(String.format("%s_ids_%s.json", fileType, fileDate));
        
        // 파일이 이미 존재하는지 확인
        if (!Files.exists(jsonFile)) {
            if (!Files.exists(gzFile)) {
                log.info("Downloading {} from: {}", fileType, url);
                downloadFile(url, gzFile);
                log.info("Downloaded: {} (size: {} bytes)", gzFile, Files.size(gzFile));
            }
            
            // 2. 압축 해제
            log.info("Decompressing {} file...", fileType);
            decompressGzipFile(gzFile, jsonFile);
            log.info("Decompressed to: {} (size: {} bytes)", jsonFile, Files.size(jsonFile));
        } else {
            log.info("Using existing file: {}", jsonFile);
        }
        
        // 3. 파싱 및 큐 적재
        log.info("Parsing and loading {} data to queue...", entityType);
        loadDataToQueue(jsonFile, entityType);
    }
    
    private void downloadFile(String urlString, Path targetPath) throws IOException {
        URL url = new URL(urlString);
        try (InputStream in = url.openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    private void decompressGzipFile(Path gzipPath, Path outputPath) throws IOException {
        try (GZIPInputStream gzis = new GZIPInputStream(Files.newInputStream(gzipPath));
             OutputStream out = Files.newOutputStream(outputPath)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }
    
    private void loadDataToQueue(Path jsonFile, String entityType) throws IOException {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        String sql = """
            INSERT INTO tmdb_work_queue (entity_type, tmdb_id, priority, status, created_at, updated_at, attempts) 
            VALUES (?, ?, ?, 'PENDING', NOW(), NOW(), 0)
            ON CONFLICT (entity_type, tmdb_id) DO NOTHING
        """;
        
        List<Object[]> batch = new ArrayList<>();
        int totalCount = 0;
        int insertedCount = 0;
        int skippedAdult = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Map<String, Object> item = mapper.readValue(line, Map.class);
                    Integer id = (Integer) item.get("id");
                    if (id == null) {
                        continue;
                    }
                    
                    // Adult content 체크 (TV와 Person은 보통 adult 필드가 없지만 혹시 있다면 스킵)
                    Boolean adult = (Boolean) item.getOrDefault("adult", false);
                    if (adult) {
                        skippedAdult++;
                        continue;
                    }
                    
                    Double popularity = (Double) item.getOrDefault("popularity", 0.0);
                    int priority = calculatePriority(popularity);
                    
                    batch.add(new Object[]{entityType, id.longValue(), priority});
                    
                    // 1000개씩 배치 처리
                    if (batch.size() >= 1000) {
                        int[] results = jdbc.batchUpdate(sql, batch);
                        insertedCount += countSuccessful(results);
                        batch.clear();
                    }
                    
                    totalCount++;
                    if (totalCount % 10000 == 0) {
                        log.info("Processed {} {} items...", totalCount, entityType);
                    }
                    
                } catch (Exception e) {
                    log.warn("Error parsing line: {}", e.getMessage());
                }
            }
            
            // 남은 배치 처리
            if (!batch.isEmpty()) {
                int[] results = jdbc.batchUpdate(sql, batch);
                insertedCount += countSuccessful(results);
            }
        }
        
        log.info("Completed {} seeding:", entityType);
        log.info("  - Total items processed: {}", totalCount);
        log.info("  - Items inserted/updated: {}", insertedCount);
        log.info("  - Adult content skipped: {}", skippedAdult);
        
        // 실제 큐 상태 확인
        Integer pendingCount = jdbc.queryForObject(
            "SELECT COUNT(*) FROM tmdb_work_queue WHERE entity_type = ? AND status = 'PENDING'",
            Integer.class, entityType
        );
        log.info("  - Current pending in queue: {}", pendingCount);
    }
    
    private int calculatePriority(Double popularity) {
        if (popularity == null) return 0;
        if (popularity > 100) return 100;
        if (popularity > 50) return 80;
        if (popularity > 20) return 60;
        if (popularity > 10) return 40;
        if (popularity > 5) return 20;
        return 10;
    }
    
    private int countSuccessful(int[] results) {
        int count = 0;
        for (int result : results) {
            // result가 1이면 성공적으로 삽입됨
            // 0이면 중복으로 인해 무시됨
            if (result > 0) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 큐 상태 확인
     */
    public Map<String, Map<String, Integer>> verifyQueueStatus() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        
        String sql = """
            SELECT entity_type, 
                   COUNT(*) as total,
                   COUNT(CASE WHEN status='PENDING' THEN 1 END) as pending,
                   COUNT(CASE WHEN status='PROCESSING' THEN 1 END) as processing,
                   COUNT(CASE WHEN status='DONE' THEN 1 END) as done,
                   COUNT(CASE WHEN status='FAILED' THEN 1 END) as failed
            FROM tmdb_work_queue
            GROUP BY entity_type
            ORDER BY entity_type
        """;
        
        Map<String, Map<String, Integer>> result = new HashMap<>();
        
        log.info("===== Queue Status =====");
        jdbc.query(sql, rs -> {
            String entityType = rs.getString("entity_type");
            Map<String, Integer> stats = new HashMap<>();
            stats.put("total", rs.getInt("total"));
            stats.put("pending", rs.getInt("pending"));
            stats.put("processing", rs.getInt("processing"));
            stats.put("done", rs.getInt("done"));
            stats.put("failed", rs.getInt("failed"));
            
            result.put(entityType, stats);
            
            log.info("Entity: {} - Total: {}, Pending: {}, Processing: {}, Done: {}, Failed: {}", 
                entityType,
                stats.get("total"),
                stats.get("pending"),
                stats.get("processing"),
                stats.get("done"),
                stats.get("failed")
            );
        });
        
        return result;
    }
}