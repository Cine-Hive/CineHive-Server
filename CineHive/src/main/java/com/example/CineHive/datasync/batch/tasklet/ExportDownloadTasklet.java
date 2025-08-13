package com.example.CineHive.datasync.batch.tasklet;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExportDownloadTasklet implements Tasklet {

    private final TmdbApiClient tmdbApiClient;
    
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM_dd_yyyy");

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // Job Parameter에서 파일 날짜 가져오기 (기본값: 어제)
        String fileDate = chunkContext.getStepContext()
                .getJobParameters()
                .getOrDefault("fileDate", getYesterdayString())
                .toString();

        log.info("TMDB Daily Export 다운로드 시작: fileDate={}", fileDate);

        try {
            // 1. Daily Export 파일 다운로드 (압축 파일)
            byte[] gzipData = tmdbApiClient.downloadDailyExport(fileDate, "movie");
            
            // 2. 임시 디렉토리 생성
            Path tempDir = Paths.get("temp");
            Files.createDirectories(tempDir);
            
            // 3. GZIP 압축 해제 및 NDJSON 파일 저장
            Path outputFile = tempDir.resolve("movie_export.json");
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(gzipData));
                 FileOutputStream fileOutputStream = new FileOutputStream(outputFile.toFile())) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
            
            // 4. ExecutionContext에 파일 경로 저장 (다음 Step에서 사용)
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .putString("exportFilePath", outputFile.toString());
            
            log.info("Daily Export 파일 다운로드 및 압축 해제 완료: {}", outputFile);
            
            return RepeatStatus.FINISHED;
            
        } catch (Exception e) {
            log.error("Daily Export 다운로드 실패: fileDate={}", fileDate, e);
            throw e;
        }
    }

    private String getYesterdayString() {
        return LocalDate.now().minusDays(1).format(FILE_DATE_FORMATTER);
    }
}