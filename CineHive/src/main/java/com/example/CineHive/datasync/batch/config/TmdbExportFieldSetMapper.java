package com.example.CineHive.datasync.batch.config;

import com.example.CineHive.datasync.dto.TmdbExportItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

@Slf4j
public class TmdbExportFieldSetMapper implements FieldSetMapper<TmdbExportItem> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TmdbExportItem mapFieldSet(FieldSet fieldSet) throws BindException {
        try {
            // NDJSON 라인을 TmdbExportItem으로 변환
            String jsonLine = fieldSet.readString("jsonLine");
            return objectMapper.readValue(jsonLine, TmdbExportItem.class);
        } catch (Exception e) {
            log.warn("TMDB Export 아이템 파싱 실패: {}", fieldSet.toString());
            throw new BindException(this, "TmdbExportItem");
        }
    }
}