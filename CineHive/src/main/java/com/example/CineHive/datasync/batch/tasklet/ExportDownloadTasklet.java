package com.example.CineHive.datasync.batch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
public class ExportDownloadTasklet implements Tasklet {

    private static final String TMDB_EXPORT_URL_PATTERN = "http://files.tmdb.org/p/exports/%s_ids_%s.json.gz";
    private static final String EXPORT_BASE_DIR = "/data/tmdb/exports";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM_dd_yyyy");

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String entityType = getEntityType(chunkContext);
        String fileDate = getFileDate(chunkContext);

        log.info("Starting download for {} export file with date: {}", entityType, fileDate);

        Path exportDir = createExportDirectory(fileDate);
        Path gzFile = downloadExportFile(entityType, fileDate, exportDir);
        Path jsonFile = decompressFile(gzFile, exportDir, entityType, fileDate);

        // Store path in JobExecutionContext for next step
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .putString("exportPath", jsonFile.toString());

        log.info("Export file successfully downloaded and decompressed: {}", jsonFile);
        log.info("Stored exportPath in JobExecutionContext: {}", jsonFile.toString());
        
        return RepeatStatus.FINISHED;
    }

    private String getEntityType(ChunkContext chunkContext) {
        Object entityType = chunkContext.getStepContext()
                .getJobParameters()
                .get("entityType");
        return entityType != null ? entityType.toString() : "movie";
    }

    private String getFileDate(ChunkContext chunkContext) {
        Object fileDate = chunkContext.getStepContext()
                .getJobParameters()
                .get("fileDate");
        
        if (fileDate == null || fileDate.toString().isBlank()) {
            // Default to yesterday's export
            return LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        }
        
        return fileDate.toString();
    }

    private Path createExportDirectory(String fileDate) throws Exception {
        Path exportDir = Paths.get(EXPORT_BASE_DIR, fileDate);
        Files.createDirectories(exportDir);
        log.debug("Export directory created/verified: {}", exportDir);
        return exportDir;
    }

    private Path downloadExportFile(String entityType, String fileDate, Path exportDir) throws Exception {
        String url = String.format(TMDB_EXPORT_URL_PATTERN, entityType, fileDate);
        Path gzFile = exportDir.resolve(String.format("%s_ids_%s.json.gz", entityType, fileDate));

        // Check if file already exists (resume capability)
        if (Files.exists(gzFile)) {
            log.info("Compressed file already exists, skipping download: {}", gzFile);
            return gzFile;
        }

        log.info("Downloading from URL: {}", url);
        
        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, gzFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        long fileSize = Files.size(gzFile);
        log.info("Downloaded file: {} (size: {} bytes)", gzFile.getFileName(), fileSize);
        
        return gzFile;
    }

    private Path decompressFile(Path gzFile, Path exportDir, String entityType, String fileDate) throws Exception {
        Path jsonFile = exportDir.resolve(String.format("%s_ids_%s.json", entityType, fileDate));
        
        // Check if already decompressed
        if (Files.exists(jsonFile)) {
            log.info("Decompressed file already exists, skipping decompression: {}", jsonFile);
            return jsonFile;
        }
        
        log.info("Decompressing file: {}", gzFile.getFileName());
        
        try (GZIPInputStream gis = new GZIPInputStream(Files.newInputStream(gzFile));
             OutputStream out = Files.newOutputStream(jsonFile, 
                     StandardOpenOption.CREATE, 
                     StandardOpenOption.TRUNCATE_EXISTING)) {
            
            byte[] buffer = new byte[8192];
            int len;
            long totalBytes = 0;
            
            while ((len = gis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                totalBytes += len;
            }
            
            log.info("Decompressed {} bytes to: {}", totalBytes, jsonFile.getFileName());
        }
        
        // Keep the compressed file for resume capability
        log.debug("Keeping compressed file for potential resume: {}", gzFile);
        
        return jsonFile;
    }
}