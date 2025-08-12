package com.example.CineHive.datasync.domain.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TvKeywordId implements Serializable {
    private Long tvId;
    private Long keywordId;
}