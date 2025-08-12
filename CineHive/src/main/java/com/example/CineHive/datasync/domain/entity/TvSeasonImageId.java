package com.example.CineHive.datasync.domain.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TvSeasonImageId implements Serializable {
    private Long seasonTmdbId;
    private String filePath;
}